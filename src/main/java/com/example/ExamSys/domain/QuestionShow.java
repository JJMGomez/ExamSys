package com.example.ExamSys.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "question_show")
public class QuestionShow implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "function_introduction")
	private String functionIntroduction;
	
	@Column(name = "technology")
	private String technology;
	
	@Column(name = "image_url")
	private String imageUrl;
	
	@Column(name = "video_url")
	private String videoUrl;
	
	@Column(name = "type")
	private String type;

    @ManyToMany(mappedBy = "showQuestions", fetch = FetchType.LAZY)
    private Set<QuestionBank> questionBankSet = new HashSet<>();

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFunctionIntroduction() {
		return functionIntroduction;
	}

	public void setFunctionIntroduction(String functionIntroduction) {
		this.functionIntroduction = functionIntroduction;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public Set<QuestionBank> getQuestionBankSet() {
        return questionBankSet;
    }

    public void setQuestionBankSet(Set<QuestionBank> questionBankSet) {
        this.questionBankSet = questionBankSet;
    }
}
