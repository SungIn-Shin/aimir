package com.aimir.util;

/**
 * 검색 조건을 담는 클래스
 * @author YeonKyoung Park(goodjob)
 *
 */
public class Condition {

    private String field; //entity field name
    private Object[] value; //between, in
    private String operator;
    private Restriction restriction;
    public enum Restriction {
        EQ, NEQ, LIKE, IN, GE, GT, LT, LE, BETWEEN, AND, OR, EMPTTY, NOTEMPTY, NOTNULL, NULL, NOT, SQL, ORDERBYDESC, 
        ORDERBY, FIRST, MAX, ALIAS, INNER_JOIN, LEFT_JOIN, NE;
    }

    public Condition() { }
    
    public Condition(String field,Object[] value,String operator,Restriction restriction) {
        this.field = field;
        this.value = value;
        this.operator = operator;
        this.restriction = restriction;
    }

    public String toString() {
        StringBuffer info = new StringBuffer();

        info
        .append("field:").append(field).append("\n")
        .append("value:").append(value).append("\n")
        .append("operator:").append(operator).append("\n")
        .append("restrictions:").append(restriction).append("\n");
        return info.toString();
    }
    
    public String getField() {
        return field;
    }
    public Object[] getValue() {
        return value;
    }
    public String getOperator() {
        return operator;
    }
    public Restriction getRestriction() {
        return restriction;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    public void setValue(Object[] value) {
        this.value = value;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public void setRestrict(Restriction restrict) {
        this.restriction = restrict;
    }
}
