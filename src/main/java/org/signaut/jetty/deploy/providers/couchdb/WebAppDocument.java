package org.signaut.jetty.deploy.providers.couchdb;

public class WebAppDocument extends Document {
    private String name;
    private String contextPath;
    private String war;
    private boolean showingFullStacktrace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getWar() {
        return war;
    }

    public void setWar(String war) {
        this.war = war;
    }

    public boolean isShowingFullStacktrace() {
        return showingFullStacktrace;
    }

    public void setShowingFullStacktrace(boolean showingFullStacktrace) {
        this.showingFullStacktrace = showingFullStacktrace;
    }

    @Override
    public String toString() {
        return "WebAppDocument [name=" + name + ", contextPath=" + contextPath + ", war=" + war + "]";
    }
       
}