package com.labelprinter.android.Models;

public class TicketModel {
    private String id;
    private String type;
    private String name;
    private int price;
    private float taxRatio;
    private int tax;
    private int rowPos;
    private int colPos;
    private String fgColor;
    private String bgColor;
    private int order;
    private int endDays;

    public TicketModel () {

    }

    public void setId(String val) {
        id = val;
    }
    public String getId() {
        return id;
    }

    public void setType(String val) {
        type = val;
    }
    public String getType() {
        return type;
    }

    public void setName(String val) {
        name = val;
    }
    public String getName() {
        return name;
    }

    public void setPrice(int val) {
        price = val;
    }
    public int getPrice() {
        return price;
    }

    public void setTaxRatio(float val) {
        taxRatio = val;
    }
    public float getTaxRatio() {
        return taxRatio;
    }

    public void setTax(int val) {
        tax = val;
    }
    public int getTax() {
        return tax;
    }

    public void setRowPos(int val) {
        rowPos = val;
    }
    public int getRowPos() {
        return rowPos;
    }

    public void setColPos(int val) {
        colPos = val;
    }
    public int getColPos() {
        return colPos;
    }

    public void setFgColor(String val) {
        fgColor = val;
    }
    public String getFgColor() {
        return fgColor;
    }

    public void setBgColor(String val) {
        bgColor = val;
    }
    public String getBgColor() {
        return bgColor;
    }

    public void setOrder(int val) {
        order = val;
    }
    public int getOrder() {
        return order;
    }

    public void setEndDays(int val) {
        endDays = val;
    }
    public int getEndDays() {
        return endDays;
    }
}
