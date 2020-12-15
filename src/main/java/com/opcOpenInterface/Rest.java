package com.opcOpenInterface;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimerTask;
import org.jfree.data.time.Millisecond;
import com.opcOpenInterface.type.Label;
import com.opcOpenInterface.type.Trend;
import org.jfree.data.time.TimeSeries;

public class Rest extends TimerTask {
    private LinkedList<Trend> trends = new LinkedList();
    private LinkedList<Label> labels = new LinkedList();
    private String registeredVariable;
    private TimeSeries serie;

    public Rest() {
    }

    public void addVariable(Trend trend) {
        this.trends.add(trend);
    }

    public void addLabel(Label label) {
        this.labels.add(label);
    }

    public void setVariable(String tagName, TimeSeries serie) {
        this.registeredVariable = tagName;
        this.serie = serie;
    }

    public JsonObject getVariable(String variable) throws Exception {
        if(variable == null) {
            return null;
        } else {
            String USER_AGENT = "Mozilla/5.0";
            //String url = "http://127.0.0.1:4567/opc/" + variable.replace(".", "/");
            String url = "http://10.1.100.119:4567/opc/" + variable.replace(".", "/");
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection)obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            //System.out.println(response.toString());
            JsonObject objet = (new JsonParser()).parse(response.toString()).getAsJsonObject();
            //System.out.println(objet.get("value").getAsString());
            return objet;
        }
    }

    public JsonObject setVariable(String variable) throws Exception {
        if(variable == null) {
            return null;
        } else {
            String USER_AGENT = "Mozilla/5.0";
            //String url = "http://127.0.0.1:4567/opc/" + variable.replace(".", "/");
            String url = "http://10.1.100.119:4567/opc/" + variable.replace(".", "/");
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection)obj.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            //System.out.println(response.toString());
            JsonObject objet = (new JsonParser()).parse(response.toString()).getAsJsonObject();
            //System.out.println(objet.get("value").getAsString());
            return objet;
        }
    }

    public void run() {
        Iterator var2 = this.trends.iterator();

        JsonObject o;
        String value;
        while(var2.hasNext()) {
            Trend t = (Trend)var2.next();

            try {
                o = this.getVariable(t.getTag());
                if(o == null) {
                    return;
                }

                value = o.get("value").getAsString();
                if(value != null) {
                    value = value.replace("false", "0").replace("true", "1");
                    t.getSerie().add(new Millisecond(), Double.parseDouble(value));
                    t.getSerie().setDescription(o.get("comment").getAsString());
                }
            } catch (Exception var6) {
                System.out.println(var6.getMessage());
            }
        }

        var2 = this.labels.iterator();

        while(var2.hasNext()) {
            Label l = (Label)var2.next();

            try {
                o = this.getVariable(l.getTag());
                if(o == null) {
                    return;
                }

                value = o.get("value").getAsString();
                if(value != null) {
                    value = value.replace("false", "0").replace("true", "1");
                    l.getjLabel().setText(l.getName());
                    l.getjLabel().setForeground(value == "1"?Color.green:Color.red);
                }
            } catch (Exception var5) {
                System.out.println(var5.getMessage());
            }
        }

    }
}