package likelion13th.voyageaider.controller;

import likelion13th.voyageaider.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/chat")
    public String chatPage(@AuthenticationPrincipal User user) {
        return "chat";
    }
}