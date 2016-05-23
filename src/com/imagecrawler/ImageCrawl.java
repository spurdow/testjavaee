package com.imagecrawler;

import com.imagecrawler.utils.ImageProgress;
import org.apache.commons.validator.routines.UrlValidator;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;


import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by davidluvellejoseph on 5/22/16.
 */
@ManagedBean(name = "imageCrawl")
@SessionScoped
public class ImageCrawl implements Serializable{
    private String url = "http://www.facebook.com";
    private ExecutorService mService = Executors.newFixedThreadPool(50);

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void start() {
        int counter = 0;
        Elements img = null;
        try {
            Document doc = Jsoup.connect(url).get();

            // Get all img tags
            img = doc.getElementsByTag("img");
            for(final Element el : img) {

                showMessage(el);

            }

            System.out.println("Url " + img.get(0).absUrl("src"));
            counter = img.size();
        }catch(IOException e){
            FacesMessage msg= new FacesMessage("Failed " +  e);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage("msgs",
                    msg);
            return;
        }
        FacesContext.getCurrentInstance().addMessage("msgs",
                new FacesMessage("Total Images : " +  counter));
        startDownload(img);

        FacesContext.getCurrentInstance().addMessage("msgs",
                new FacesMessage("Done..!"));

    }

    synchronized void startDownload(final Elements images){
        for(final Element image : images) {
            mService.execute(new Runnable() {
                @Override
                public void run() {
                    downloadImage(image.absUrl("src"));
                }
            });
        }
    }

    public static final String CHANNEL = "/notify";
    void showMessage(Element el){
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish(CHANNEL, new FacesMessage(StringEscapeUtils.escapeHtml(el.attr("alt")), StringEscapeUtils.escapeHtml(el.absUrl("src"))));

    }

    private void downloadImage(final String src) {
        Collection<Future<?>> futures = new LinkedList<>();
        futures.add(mService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    new ImageProgress().download(src);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        // until fiish
        for(Future<?> f : futures){
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


    }
}
