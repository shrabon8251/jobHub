package com.example.jobhub.controller.auth;

import com.example.jobhub.dto.form.LoginForm;
import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        LoginForm form = new LoginForm();
        addLoginValidationError(model, session, form);
        model.addAttribute("loginForm", form);
        return "auth/login";
    }

    @RequestMapping(value = "/access-denied", method = {RequestMethod.GET, RequestMethod.POST})
    public String accessDenied() {
        return "error/access-denied";
    }

    private void addLoginValidationError(Model model, HttpSession session, LoginForm form) {
        String message = (String) session.getAttribute("loginValidationMessage");
        String field = (String) session.getAttribute("loginValidationField");
        if (message == null || field == null) {
            return;
        }
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "loginForm");
        result.rejectValue(field, "invalid", message);
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "loginForm", result);
        session.removeAttribute("loginValidationMessage");
        session.removeAttribute("loginValidationField");
    }
}
