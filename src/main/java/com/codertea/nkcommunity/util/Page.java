package com.codertea.nkcommunity.util;
/**
 * 封装分页相关的信息
 * */
public class Page {

    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数（用于计算总页数）
    private int rows;
    // 查询路径（用于复用分页链接）
    private String path;

    /*
    * 计算当前页的起始行的索引
    * */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /*
    * 获取总页数
    * */
    public int getTotal() {
        if(rows % limit == 0) return rows / limit;
        else return rows / limit + 1;
    }

    /*
    * 显示页码时，不可能显示所有的页码，因为可能有上百页。
    * 通常会显示当前页附近的几页，我这里是自定义的规则，显示当前页前面和后面的2页
    * 即页码边界是from 和 to
    * */
    public int getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return Math.min(to, total);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1) this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100) this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }
}
