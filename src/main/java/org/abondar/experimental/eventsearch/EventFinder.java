/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.abondar.experimental.eventsearch;


import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

/**
 *
 * @author alex
 */
public class EventFinder {

    private Document doc;
    private final HashMap<String, String> eventTypes;

    public EventFinder() {
        eventTypes = new HashMap<>();
        eventTypes.put("theatre", "Teaтры");
        eventTypes.put("lectures", "Лекции");
        eventTypes.put("club", "Клубы");
        eventTypes.put("art", "Выставки");
        eventTypes.put("concert", "Концерты");

    }

    ;
    
    

    public void getCategorizedEvents(String type) {
        try {

            doc = Jsoup.connect("https://afisha.yandex.ru/msk/events/?category=" + type + "&limit=1000").get();

            Elements els = doc.select("a[href]");

            for (Element e : els) {

                for (Attribute attr : e.attributes().asList()) {

                    if (attr.getValue().contains("clck.yandex.ru")) {

                        if (attr.getValue().charAt(97) != '/') {
                            getEvent(attr.getValue().substring(90, 96), type);

                        } else {
                            getEvent(attr.getValue().substring(90, 97), type);

                        }
                    }
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(EventFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getEvent(String eventId, String evType) {
        try {

            Document dc = Jsoup.connect("https://afisha.yandex.ru/msk/events/" + eventId + "/").get();

            Event eb = new Event();
            eb.setEventID(eventId);
            eb.setCategory(eventTypes.get(evType));
            Elements elems = dc.select("meta");

            for (Element e : elems) {
                if (e.attributes().get("property").contains("og:description")) {
                    eb.setDescription(e.attributes().get("content"));

                }

            }

            elems = dc.select("title");

            for (Element e : elems) {

                eb.setName(e.html().substring(0, e.html().indexOf("–")));
            }

            elems = dc.select("a[href]");

            for (Element e : elems) {

                for (Attribute attr : e.attributes().asList()) {

                    if (attr.getValue().contains("/msk/places/")) {

                        eb.setPlace(getEventPlaces(attr.getValue()));

                    }
                }

            }

            elems = dc.select("tr[id]");

            for (Element e : elems) {
                for (Attribute attr : e.attributes().asList()) {

                    if (attr.getValue().contains("f")) {

                        eb.setDate(e.children().first().html());

                        try {
                            Element e1 = e.child(1).children().first();
                            Element e2 = e1.children().first();
                            Element e3 = e2.children().first();
                            Element e4 = e3.children().first();

                            eb.setTime(e4.html());

                        } catch (NullPointerException ex) {

                            Element e1 = e.child(2).children().first();
                            Element e2 = e1.children().first();
                            Element e3 = e2.children().first();
                            Element e4 = e3.children().first();
                            eb.setTime(e4.html());
                        }
                    }
                }

            }

            geoCode(eb);
             formJson(eb);
            
        } catch (IOException ex) {
            Logger.getLogger(EventFinder.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getEventPlaces(String place) {

        String res = "";
        try {
            Document placeDoc = Jsoup.connect("https://afisha.yandex.ru" + place).get();

            Elements elems = placeDoc.select("p");

            for (Element e : elems) {

                if (e.parents().get(1).html().contains("<div style")) {

                    if (e.children().size() > 1) {
                        if (e.child(1).hasAttr("href")) {
                            res = e.child(1).html() + " Москва";

                        }
                    } else if (e.children().isEmpty()) {
                        res = e.html() + " Москва";
                    }
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(EventFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public void formJson(Event eb) {

        File fil = new File("/home/abondar/EventSearch/jsons/" + eb.getEventID() + ".json");
        try {
            FileOutputStream fos = new FileOutputStream(fil);
            ObjectMapper om = new ObjectMapper();
            om.writeValue(fos, eb);

        } catch (IOException ex) {
            Logger.getLogger(EventFinder.class.getName()).log(Level.SEVERE, null, ex);

        }


}

      public void geoCode(Event eb) {
        try{
          String addr = eb.getPlace().replace(" ", "%20"); 
            
          URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address="+addr);
          URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null){
                   
                    sb.append(inputLine);
                   
                }
   
          eb.setGeoPos(sb.toString());
      
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
