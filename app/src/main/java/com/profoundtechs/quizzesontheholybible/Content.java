package com.profoundtechs.quizzesontheholybible;

public class Content {
    int id;
    String question;
    String answer;
    byte[] imaget;
    byte[] imageb;

    public Content() {
    }

    public Content(int id, String question, String answer, byte[] imaget, byte[] imageb) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.imaget = imaget;
        this.imageb = imageb;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public byte[] getImaget() {
        return imaget;
    }

    public void setImaget(byte[] imaget) {
        this.imaget = imaget;
    }

    public byte[] getImageb() {
        return imageb;
    }

    public void setImageb(byte[] imageb) {
        this.imageb = imageb;
    }
}
