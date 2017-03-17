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

import java.io.IOException;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

public class AutoBid {
    private static class BidList {
        private int currentPage;
        private String[] data;
        private String now;
        private String result;
        private int total;
    }

    private static class Bid {
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
    }

    public static void main(String... args) {
        String response = "";
        Gson gson = new Gson();  // TODO reuse gson object
        BidList bidList = gson.fromJson(response, BidList.class);
        Bid[] bids = parseBids(bidList);
        Bid[] open = Arrays.stream(bids).filter(bid -> bid.progress != 100).toArray(Bid[]::new);
        for (Bid o : bids) {
            System.out.println(o.progress);
        }
        for (int i = 0; i < 100; i++) {
            System.out.println(salt(4));
        }
    }

    static private Bid[] parseBids(BidList bidList) {
        String t = Base64.getEncoder().encodeToString(Integer.toString(bidList.total).getBytes());
        String d = bidList.data[0];
        int w = dayOfWeek();

        String l = d.substring(0, w) + d.substring(w + t.length(), d.length() - w - t.length()) + d.substring(d
                .length() - w);
        for (int i = 0; i < w + 1; i++) {
            l = new String(Base64.getDecoder().decode(l));
        }

        System.out.println(l);
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
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());  // TODO
        // remove this
        HttpClient client = HttpClients.custom().setRoutePlanner(routePlanner).build();
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

    static private void purchase() {
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost("https://www.lljr.com/manageMoney/tenderFreeze");
        Set<NameValuePair> nvps = new TreeSet<>();
        nvps.add(new BasicNameValuePair("username", "vip"));
        nvps.add(new BasicNameValuePair("password", "secret"));
        request.setEntity(new UrlEncodedFormEntity(nvps));
    }

    static private String salt(int l) {  // TODO replace this with more efficient salt
        String t = "poiuytrewqasdfghjklmnbvcxzQWERTYUIPLKJHGFDSAZXCVBNM";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; i++) {
            sb.append(t.charAt((int) (t.length() * Math.random())));
        }
        return sb.toString();
    }
}
