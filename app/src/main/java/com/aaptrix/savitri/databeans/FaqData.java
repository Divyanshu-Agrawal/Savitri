package com.aaptrix.savitri.databeans;

import java.io.Serializable;

public class FaqData implements Serializable {

	private String question, answer;
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getAnswer() {
		return answer;
	}
	
	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
