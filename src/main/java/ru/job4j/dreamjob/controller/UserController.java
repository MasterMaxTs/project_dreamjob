package ru.job4j.dreamjob.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.userservice.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class UserController implements ManageSession {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("user")
    public User getUser(HttpSession session) {
        return getUserFromSession(session);
    }

    @GetMapping("/addUser")
    public String add() {
        return "user/addUser";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute User user,
                               RedirectAttributes redirectAttributes) {
        Optional<User> regUser = userService.add(user);
        if (regUser.isEmpty()) {
           String message = "Пользователь с почтой " + user.getEmail() + " "
                            + "уже существует!";
           redirectAttributes.addAttribute("msg", message);
            return "redirect:/fail";
        }
        redirectAttributes.addAttribute("user", user.getName());
        return "redirect:/success";
    }

    @GetMapping("/success")
    public String informToSuccess(@RequestParam("user") String userName,
                                  Model model) {
        model.addAttribute("name", userName);
        return "user/registration_success";
    }

    @GetMapping("/fail")
    public String informToFail(@RequestParam("msg") String msg,
                               Model model) {
        model.addAttribute("message", msg);
        return "user/registration_fail";
    }

    @GetMapping("/loginPage")
    public String loginPage(Model model,
                            @RequestParam(value = "fail", required = false) Boolean fail) {
        model.addAttribute("fail", fail != null);
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest req) {
        Optional<User> userDb =
                        userService.findUserByEmailAndPwd(
                                user.getEmail(), user.getPassword()
                        );
        if (userDb.isEmpty()) {
            redirectAttributes.addAttribute("fail", true);
            return "redirect:/loginPage";
        }
        HttpSession session = req.getSession();
        session.setAttribute("user", userDb.get());
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/loginPage";
    }
}
