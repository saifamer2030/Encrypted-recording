package app.com.smartrec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ${cosmic} on 8/13/18.
 */
public class recRecievedModel {

    private String recCloudPath;
    private String SenderUId;
    private Long sendDate;
    private String senderPhone;
    private String senderLoc;
    private String senderLoc2;
    private String recEncryKey;
    private String recFilename;

    public String getRecCloudPath() {
        return recCloudPath;
    }

    public void setRecCloudPath(String recCloudPath) {
        this.recCloudPath = recCloudPath;
    }

    public String getSenderUId() {
        return SenderUId;
    }

    public void setSenderUId(String senderUId) {
        SenderUId = senderUId;
    }

    public Long getSendDate() {
        return sendDate;
    }

    public void setSendDate(Long sendDate) {
        this.sendDate = sendDate;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderLoc() {
        return senderLoc;
    }

    public void setSenderLoc(String senderLoc) {
        this.senderLoc = senderLoc;
    }

    public String getSenderLoc2() {
        return senderLoc2;
    }

    public void setSenderLoc2(String senderLoc2) {
        this.senderLoc2 = senderLoc2;
    }

    public String getRecEncryKey() {
        return recEncryKey;
    }

    public void setRecEncryKey(String recEncryKey) {
        this.recEncryKey = recEncryKey;
    }

    public String getRecFilename() {
        return recFilename;
    }

    public void setRecFilename(String recFilename) {
        this.recFilename = recFilename;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("recCloudPath", recCloudPath);
        result.put("SenderUId", SenderUId);
        result.put("senderPhone", senderPhone);
        result.put("senderLong", senderLoc);
        result.put("senderLat", senderLoc2);
        result.put("sendDate", sendDate);

        return result;
    }
}
