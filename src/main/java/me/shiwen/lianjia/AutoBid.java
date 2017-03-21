package me.shiwen.lianjia;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class AutoBid {
    private static final Logger LOG = LoggerFactory.getLogger(AutoBid.class);

    private static class BidList {
        private int currentPage;
        private String[] data;
        private String now;
        private String result;
        private int total;
    }

    private static class Bid implements Comparable<Bid> {
        private String acreage;
        private String bidDeadline;
        private String bidEndTime;
        private int bidSort;
        private int bidStatus;
        private int buyNum;
        private String commonBorrower;
        private String commonBorrowerCompany;
        private String description;
        private String duration;
        private String familyAssets;
        private String fsTime;
        private String fullscaleTime;
        private int fullscaleTimeSort;
        private String householdDebt;
        private int id;
        private String issuingTime;
        private String issuingTimeHour;
        private String issuingTimeSort;
        private float lenderyearRate;
        private int loanPeriod;
        private String loanTermUnit;
        private String loanTotal;
        private float loanTotalDouble;
        private String location;
        private int maritalStatus;
        private String market;
        private String name;
        private String personCreditDescription;
        private String predictBenefitTime;
        private int progress;
        private String reason;
        private float remainInvestAmount;
        private int repaySource;
        private int risk;
        private String riskAssessment;
        private int sex;
        private int standbyField;
        private String title;
        private String type;

        @Override
        public int compareTo(Bid bid) {
            return this.id - bid.id;
        }
    }

    public static void main(String... args) {
        //        String response = "";
        //        Gson gson = new Gson();  // TODO reuse gson object
        //        BidList bidList = gson.fromJson(response, BidList.class);
        //        Bid[] bids = parseBids(bidList);
        //        Bid[] open = Arrays.stream(bids).filter(bid -> bid.progress != 100).toArray(Bid[]::new);
        //        for (Bid o : bids) {
        //            LOG.debug(o.progress);
        //        }
        //        for (int i = 0; i < 100; i++) {
        //            LOG.debug(salt(4));
        //        }

        LOG.debug("it all began");
        Set<Bid> bidSet = new TreeSet<>();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Bid[] bids = openBids();

                for (Bid bid : bids) {
                    if (!bidSet.contains(bid)) {
                        LOG.debug("id: " + bid.id);
                        LOG.debug("time: " + bid.issuingTime);
                        bidSet.add(bid);
                        schedule(bid);
                    }
                }
            }
        }, 0, 5 * 1000);
    }

    static private Bid[] parseBids(BidList bidList) {
        String t = Base64.getEncoder().encodeToString(Integer.toString(bidList.total).getBytes());
        String d = bidList.data[0];
        int w = dayOfWeek();

        String l = d.substring(0, w) + d.substring(w + t.length(), d.length() - w - t.length()) +
                d.substring(d.length() - w);
        for (int i = 0; i < w + 1; i++) {
            l = new String(Base64.getDecoder().decode(l));
        }

        Gson gson = new Gson();
        return gson.fromJson(l, Bid[].class);
    }

    static private int dayOfWeek() {
        int d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return d > 1 ? d - 1 : 7;
    }

    static private String handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }

    static private Bid[] openBids() {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://www.lljr.com/manageMoney/sort?" +
                "sortType=9&page=1&num=10&fromtype=1&t=tCXcbuYZ");

        String response = null;
        try {
            response = client.execute(request, AutoBid::handleResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();  // TODO reuse gson object
        BidList bidList = gson.fromJson(response, BidList.class);
        Bid[] bids = parseBids(bidList);
        return Arrays.stream(bids).filter(bid -> bid.bidSort > 1).toArray(Bid[]::new);
    }

    static private void schedule(Bid bid) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // TODO make sure it is correct; make it constant
        Date issuingTime;
        try {
            issuingTime = df.parse(bid.issuingTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Timer timer = new Timer();  // TODO timer reuse?
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                purchase(bid.id);
            }
        }, issuingTime);
    }

    static private void purchase(int bidId) {
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost("https://www.lljr.com/manageMoney/tenderFreeze");
        Set<NameValuePair> nvps = postData("15000000000", "password", 100000, bidId, 0);
        request.setEntity(new UrlEncodedFormEntity(nvps));
        String response = null;
        try {
            response = client.execute(request, AutoBid::handleResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.debug(response);
    }

    static private String salt() {  // TODO replace this with more efficient salt
        String t = "poiuytrewqasdfghjklmnbvcxzQWERTYUIPLKJHGFDSAZXCVBNM";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(t.charAt((int) (t.length() * Math.random())));
        }
        return sb.toString();
    }

    static private String uglify(String s) {
        int c = (int) (Math.random() * 6 + 4);
        for (int i = 0; i < c; i++) {
            if (i == c - 1) {
                s = Integer.toString(c) + s;
            }
            s = new String(Base64.getEncoder().encode(s.getBytes()));
        }
        return s;
    }

    static private Set<NameValuePair> postData(String user, String password, int amount, int bidId, int cashCoupon) {
        String n = salt() + user + salt();
        String s = "payKey=" + password + "&investment=" + amount + "&bidId=" + bidId + "&cashCoupon=" + cashCoupon;
        String m = null;
        try {
            m = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Set<NameValuePair> nvps = new TreeSet<>();
        nvps.add(new BasicNameValuePair("_n", uglify(n)));
        nvps.add(new BasicNameValuePair("_m", uglify(m)));
        return nvps;
    }
}
