package data.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HtmlController {
    @GetMapping("/")
    public String index() {
        return "layout/index";
    }

    @GetMapping("/login")
    public String login() { return "layout/login"; }

    @GetMapping("/draft/approvalDone")
    public String approvlDone() {
        return "layout/approval/approval_done";
    }

    @GetMapping("/draft/approvalDoneFile/{id}")
    public String approvalDoneFile(@PathVariable int id) {
        return "layout/approval/approval_done_file";
    }

    @GetMapping("/draft/approvalDoneFileWrite/{id}")
    public String approvalDoneFileWrite(@PathVariable int id) {
        return "layout/approval/approval_done_file_write";
    }

    @GetMapping("/draft/approvalInbox")
    public String approvalInbox() {
        return "layout/approval/approval_inbox";
    }

    @GetMapping("/draft/approvalInboxFile/{id}")
    public String approvalInboxFile(@PathVariable int id) {
        return "layout/approval/approval_inbox_file";
    }

    @GetMapping("/draft/approvalOutBox")
    public String approvalOutBox() {
        return "layout/approval/approval_outbox";
    }

    @GetMapping("/draft/approvalOutBoxFile/{id}")
    public String approvalOutBoxFile(@PathVariable int id) {
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

    @GetMapping("/admin/managementApproval")
    public String managementApproval() { return "layout/management/management_approval"; }

    @GetMapping("/admin/managementFormer")
    public String managementFormer() { return "layout/management/management_former_employees"; }

    @GetMapping("/message/messageInBox")
    public String messageInBox() { return "layout/message/message_inbox"; }

    @GetMapping("/message/messageOutBox")
    public String messageOutBox() { return "layout/message/message_outbox"; }

    @GetMapping("/message/storage")
    public String storage() { return "layout/storage/storage"; }

    @GetMapping("/organization")
    public String organization() {
        return "error/ondaming";
    }

    @GetMapping("/alarm")
    public String alarmList() { return "alarm/alarmlist"; }

    @GetMapping("/errorpage")
    public String error() { return "error/error"; }

    @GetMapping("/ing")
    public String ondaming() { return "error/ondaming"; }
}
