package com.opcOpenInterface.type;

import javax.swing.JLabel;

public class Label {
    private String name;
    private String tag;
    private JLabel jLabel;

    public Label(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public JLabel getjLabel() {
        return this.jLabel;
    }

    public void setjLabel(JLabel jLabel) {
        this.jLabel = jLabel;
    }
}
