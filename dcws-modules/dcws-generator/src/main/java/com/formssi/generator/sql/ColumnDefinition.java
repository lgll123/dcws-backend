package com.formssi.generator.sql;

import com.formssi.generator.converter.ColumnTypeConverter;
import com.formssi.generator.util.FieldUtil;
import com.formssi.generator.util.SqlTypeUtil;
import com.formssi.generator.util.StringUtil;
import org.apache.commons.lang.StringUtils;

/**
 * 表字段信息
 */
public class ColumnDefinition {

    /**
     * 数据库字段名
     */
    private String columnName;
    /**
     * 数据库类型
     */
    private String type;
    /**
     * 是否自增
     */
    private Boolean isIdentity;
    /**
     * 是否主键
     */
    private Boolean isPk;
    /**
     * 字段注释
     */
    private String comment;
    /**
     * 字段长度
     */
    private Integer maxLength;
    /**
     * 小数位长度
     */
    private Integer scale;

    /**
     * 字段是否允许为null
     */
    private boolean isNullable;

    private ColumnTypeConverter columnTypeConverter;

    public void setColumnTypeConverter(ColumnTypeConverter columnTypeConverter) {
        this.columnTypeConverter = columnTypeConverter;
    }

    /**
     * 数据库字段名首字母小写
     * @return
     */
    public String getColumnNameLF() {
        return FieldUtil.lowerFirstLetter(this.columnName);
    }

    public String getLabel() {
        return StringUtils.isNotBlank(comment) ? comment : getNameCamel();
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getScale() {
        return scale;
    }

    /**
     * 获得基本类型,int,float
     *
     * @return 返回基本类型
     */
    public String getFieldType() {
        return getColumnTypeConverter().convertType(type);
    }

    /**
     * 获得基本类型,int,float
     *
     * @return 返回基本类型
     */
    public String getTypeBase() {
        return getFieldType();
    }

    /**
     * 获得装箱类型,Integer,Float
     *
     * @return 返回装箱类型
     */
    public String getFieldTypeBox() {
        return getColumnTypeConverter().convertTypeBox(getType());
    }

    /**
     * 获得装箱类型,Integer,Float
     *
     * @return 返回装箱类型
     */
    public String getTypeBox() {
        return getFieldTypeBox();
    }

    public String getMybatisJdbcType() {
        return SqlTypeUtil.TYPE_MYBATIS_MAP.getOrDefault(getType(), "VARCHAR");
    }

    /**
     * 返回java字段名,并且第一个字母大写
     *
     * @return 返回字段名
     * @see #getNamePascal()
     */
    @Deprecated
    public String getJavaFieldNameUF() {
        return FieldUtil.upperFirstLetter(getJavaFieldName());
    }


    //   ---- name start ----

    /**
     * 返回表字段名
     * @return
     */
    public String getName() {
        return getColumnName();
    }

    /**
     * 返回java字段
     *
     * @return 返回java字段
     * @see #getNameCamel()
     */
    @Deprecated
    public String getJavaFieldName() {
        String fieldName = FieldUtil.underlineFilter(getColumnName());
        if(StringUtils.isEmpty(fieldName)){
            return fieldName;
        }
        fieldName = fieldName.replaceAll("_", "");
        return FieldUtil.lowerFirstLetter(fieldName);
    }

    /**
     * 小驼峰命名，如：userAge
     * @return
     */
    public String getNameCamel() {
        return getJavaFieldName();
    }

    /**
     * 大驼峰命名，如：UserAge
     * @return
     */
    public String getNamePascal() {
        return FieldUtil.upperFirstLetter(getJavaFieldName());
    }

    /**
     * 蛇形命名，如：user_age
     * @return
     */
    public String getNameSnake() {
        String name = getColumnName();
        name = StringUtil.trimTrailingCharacter(name, '_');
        name = StringUtil.trimLeadingCharacter(name, '_');
        return name.toLowerCase();
    }

    /**
     * 蛇形命名大写，如：USER_AGE
     * @return
     */
    public String getNameSnakeBig() {
        return getNameSnake().toUpperCase();
    }

    /**
     * 烤串命名，如：user-name
     * @return
     */
    public String getNameKebab() {
        String name = this.getNameSnake();
        return name.replace('_', '-');
    }

    /**
     * 烤串命名大写，如：USER-AGE
     * @return
     */
    public String getNameKebabBig() {
        return this.getNameKebab().toUpperCase();
    }

    /**
     * 帕斯卡蛇形命名，如：User_Age
     * @return
     */
    public String getNamePascalSnake() {
        String fieldNameSnake = this.getNameSnake();
        String[] split = fieldNameSnake.split("_");
        for (int i = 0; i < split.length; i++) {
            split[i] = FieldUtil.upperFirstLetter(split[i]);
        }
        return String.join("_", split);
    }

    /**
     * 帕斯卡烤串命名，如：User-Age
     * @return
     */
    public String getNamePascalKebab() {
        String fieldNameSnake = this.getNameSnake();
        String[] split = fieldNameSnake.split("_");
        for (int i = 0; i < split.length; i++) {
            split[i] = FieldUtil.upperFirstLetter(split[i]);
        }
        return String.join("-", split);
    }

    /**
     * 获得基本类型,int,float
     *
     * @return 返回基本类型
     */
    public String getJavaType() {
        return getFieldType();
    }

    /**
     * 获得装箱类型,Integer,Float
     *
     * @return 返回装箱类型
     */

    public String getJavaTypeBox() {
        return getFieldTypeBox();
    }


    /**
     * 是否是自增主键
     *
     * @return true, 是自增主键
     */
    public boolean getIsIdentityPk() {
        return getIsPk() && getIsIdentity();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsIdentity() {
        return isIdentity;
    }

    public void setIsIdentity(Boolean isIdentity) {
        this.isIdentity = isIdentity;
    }

    public Boolean getIsPk() {
        return isPk;
    }

    public void setIsPk(Boolean isPk) {
        this.isPk = isPk;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null) {
            comment = "";
        }
        this.comment = comment;
    }

    public ColumnTypeConverter getColumnTypeConverter() {
        return columnTypeConverter;
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }
}
