package com.farkas.projectjsf.namedbeans;

import com.farkas.projectjsf.entities.AppUser;
import com.farkas.projectjsf.entities.Msg;
import com.farkas.projectjsf.sessionbeans.MsgService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 * @author attila
 */
@Named(value = "msgServices")
@ViewScoped
public class MsgServiceBean implements Serializable {

    @EJB
    private MsgService service;

    @Inject
    private LogServiceBean logService;

    private List<Msg> inMessages;
    private List<Msg> outMessages;
    private List<AppUser> recipients;

    private Msg message;
    private Long userId;
    private String mode;

    @PostConstruct
    public void init() {
        inMessages = new ArrayList<>();
        outMessages = new ArrayList<>();
        recipients = new ArrayList<>();

        message = new Msg();
        mode = "N";

        if (logService.getAppUser() != null) {
            userId = logService.getAppUserId();
            this.readMessages();
            this.readMsgRecipients();
        }

    }

    public void readMessages() {
        inMessages.clear();
        outMessages.clear();
        try {
            List<Msg> inMsg = service.readInMsgByUser(logService.getAppUser());
            List<Msg> outMsg = service.readOutMsgByUser(logService.getAppUser());
            if (inMsg != null && outMsg != null) {
                inMessages.addAll(inMsg);
                outMessages.addAll(outMsg);
            }
        } catch (EJBException e) {
        }
        System.out.println("readMessages");
    }

    public void readMsgRecipients() {
        recipients.clear();
        try {
            List<AppUser> userDB = service.readMsgRecipients(userId);
            if (userDB != null) {
                recipients.addAll(userDB);
                System.out.println("readAllUsers " + this.toString());
            }
        } catch (EJBException e) {
            // hibaüzenet a sikertelen beolvasásrol!!!!!!!!!!
        }
    }

    public void saveMessage(ActionEvent e) {
        // save message to DB
        RequestContext reqContext = RequestContext.getCurrentInstance();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            service.saveMessage(message);
            reqContext.addCallbackParam("saveFailed", false);
            this.readMessages();
        } catch (EJBException ex) {
            reqContext.addCallbackParam("saveFailed", true);
            String errStr = "Save failed.";
            facesContext.addMessage("growl-msg",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, errStr, errStr));
        }
    }

    public void viewMessage(Msg msg) {
        mode = "V";
        if (!msg.isReadflag() && msg.getTouser().equals(logService.getAppUser())) {
            msg.setReadflag(true);
            service.modifyReadFlag(msg);
        }
        message = msg;
    }

    public void newMessage() {
        mode = "N";
        message = new Msg();
        message.setFromuser(logService.getAppUser());
        message.setMsgdate(new Date());
        message.setReadflag(false);
    }

    public boolean isViewMode() {
        return mode.equals("V");
    }

    // GETTERS & SETTERS
    public List<Msg> getInMessages() {
        return inMessages;
    }

    public void setInMessages(List<Msg> inMessages) {
        this.inMessages = inMessages;
    }

    public List<Msg> getOutMessages() {
        return outMessages;
    }

    public void setOutMessages(List<Msg> outMessages) {
        this.outMessages = outMessages;
    }

    public Msg getMessage() {
        return message;
    }

    public void setMessage(Msg message) {
        this.message = message;
    }

    public List<AppUser> getRecipients() {
        return recipients;
    }

}
