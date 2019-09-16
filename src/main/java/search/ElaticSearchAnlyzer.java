package search;

import java.lang.annotation.*;

@Target(ElementType.FIELD)//作用范围 字段
@Retention(RetentionPolicy.RUNTIME)//生命周期 编译字节码也要保留
@Documented //文档类型 公共的api
@Inherited //自动继承源注释
public @interface ElaticSearchAnlyzer {
    /**
     * 指定索引类型
     * ik_smart 粗粒度分词           ==>  搜索使用粗粒度分词
     * ik_max_word 细粒度分词    ==> 索引使用细粒度分词
     * standard  标准分词
     *
     * @return
     */
    String analyzer() default "";

    /**
     * 索引字段类型
     * keyword  :    存储数据时候，不会分词建立索引
     * text         :    存储数据时候，会自动分词，并生成索引
     *
     * @return type
     */
    String type() default "text";
}
