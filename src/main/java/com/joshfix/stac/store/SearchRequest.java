package com.joshfix.stac.store;

public class SearchRequest {
    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public String[] getCollections() {
        return collections;
    }

    public void setCollections(String[] collections) {
        this.collections = collections;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    private double[] bbox;
    private String time;
    private String query;
    private Integer limit;
    private Integer page;
    private String[] ids;
    private String[] collections;
    private String[] fields;

    public SearchRequest bbox(double[] bbox) {
        setBbox(bbox);
        return this;
    }

    public SearchRequest time(String time) {
        setTime(time);
        return this;
    }

    public SearchRequest query(String query) {
        setQuery(query);
        return this;
    }

    public SearchRequest limit(Integer limit) {
        setLimit(limit);
        return this;
    }

    public SearchRequest page(Integer page) {
        setPage(page);
        return this;
    }

    public SearchRequest ids(String[] ids) {
        setIds(ids);
        return this;
    }

    public SearchRequest collections(String[] collections) {
        setCollections(collections);
        return this;
    }

    public SearchRequest fields(String[] fields) {
        setFields(fields);
        return this;
    }
}
