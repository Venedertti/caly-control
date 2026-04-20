package com.calycontrol.otica.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final BruteForceProtectionListener bruteForce;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        String ip = resolveIp(request);
        if (bruteForce.isIpBlocked(ip)) {
            long minutos = (bruteForce.ipSecondsRemaining(ip) / 60) + 1;
            model.addAttribute("bloqueado",
                "Muitas tentativas. Tente novamente em " + minutos + " minuto(s).");
        }
        return "login";
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
