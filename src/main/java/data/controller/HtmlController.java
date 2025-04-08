package data.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "layout/login";
    }

    @GetMapping("/draft/approvalDone")
    public String approvlDone() {
        return "layout/approval/approval_done";
    }

    @GetMapping("/draft/approvalDoneFile")
    public String approvalDoneFile() {
        return "layout/approval/approval_done_file";
    }

    @GetMapping("/draft/approvalInbox")
    public String approvalInbox() {
        return "layout/approval/approval_inbox";
    }

    @GetMapping("/draft/approvalInboxFile")
    public String approvalInboxFile() {
        return "layout/approval/approval_inbox_file";
    }

    @GetMapping("/draft/approvalOutBox")
    public String approvalOutBox() {
        return "layout/approval/approval_outbox";
    }

    @GetMapping("/draft/approvalOutBoxFile")
    public String approvalOutBoxFile() {
        return "layout/approval/approval_outbox_file";
    }

    @GetMapping("/draft/approvalWrite")
    public String approvalWrite() {
        return "layout/approval/approval_write";
    }

    @GetMapping("/admin/management")
    public String management() {
        return "layout/management/management";
    }

    @GetMapping("/admin/managementOrg")
    public String managementOrg() {
        return "layout/management/management_org";
    }

    @GetMapping("/draft/messageInBox")
    public String messageInBox() { return "layout/message/message_inbox"; }

    @GetMapping("/organization")
    public String organization() {
        return "layout/management/organization";
    }
}
