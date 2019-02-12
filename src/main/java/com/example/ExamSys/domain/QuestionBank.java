package com.example.ExamSys.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "question_bank")
public class QuestionBank implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	//试卷难度
	@Column(name = "level")
	private int level;
	
	@ManyToMany
	@JoinTable(
			name = "bank_choice",
			joinColumns = {@JoinColumn(name = "bank_id", referencedColumnName = "id")},
				inverseJoinColumns = {@JoinColumn(name = "question_choice_id", referencedColumnName = "id")})
	private Set<QuestionChoice> choiceQuestions = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "bank_short",
			joinColumns = {@JoinColumn(name = "bank_id", referencedColumnName = "id")},
				inverseJoinColumns = {@JoinColumn(name = "question_short_id", referencedColumnName = "id")})
	private Set<QuestionShort> shortQuestions = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "bank_choice_multi",
			joinColumns = {@JoinColumn(name = "bank_id", referencedColumnName = "id")},
				inverseJoinColumns = {@JoinColumn(name = "question_choice_multi_id", referencedColumnName = "id")})
	private Set<QuestionChoice_multi> multi_choiceQuestions = new HashSet<>();
	
	@ManyToMany
	@JoinTable(
			name = "bank_show",
			joinColumns = {@JoinColumn(name = "bank_id", referencedColumnName = "id")},
				inverseJoinColumns = {@JoinColumn(name = "question_show_id", referencedColumnName = "id")})
	private Set<QuestionShow> showQuestions = new HashSet<>();

	@OneToMany(mappedBy = "questionBank", orphanRemoval = false)
	private Set<Exam_sum> exam_sums = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Set<QuestionChoice> getChoiceQuestions() {
		return choiceQuestions;
	}

	public void setChoiceQuestions(Set<QuestionChoice> choiceQuestions) {
		this.choiceQuestions = choiceQuestions;
	}

    public Set<QuestionChoice_multi> getMulti_choiceQuestions() {
        return  multi_choiceQuestions;
    }

    public void setMulti_choiceQuestions(Set<QuestionChoice_multi> multi_choiceQuestions) {
        this.multi_choiceQuestions = multi_choiceQuestions;
    }

	public Set<QuestionShort> getShortQuestions() {
		return shortQuestions;
	}

	public void setShortQuestions(Set<QuestionShort> shortQuestions) {
		this.shortQuestions = shortQuestions;
	}

	public Set<QuestionShow> getShowQuestions() {
		return showQuestions;
	}

	public void setShowQuestions(Set<QuestionShow> showQuestions) {
		this.showQuestions = showQuestions;
	}
	
	
}