package search;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TeacherVo implements Serializable {
    private static final long serialVersionUID = 7684165616892923498L;
    /**
     * 讲师id
     */
    private Long id;

    /**
     * 讲师姓名
     */
    @ElaticSearchAnlyzer(analyzer = "ik_max_word")
    private String lecturerName;

    /**
     * 讲师图片地址
     */
    private String pic;


    /**
     * 讲师介绍
     */
    @ElaticSearchAnlyzer(analyzer = "keyword")
    private String lecturerIntro;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    /**
     * 时薪
     */
    private BigDecimal price;

    /**
     * 运动
     */
    private List<String>sports;

    private Long[]ids;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getLecturerIntro() {
        return lecturerIntro;
    }

    public void setLecturerIntro(String lecturerIntro) {
        this.lecturerIntro = lecturerIntro;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getSports() {
        return sports;
    }

    public void setSports(List<String> sports) {
        this.sports = sports;
    }
}
