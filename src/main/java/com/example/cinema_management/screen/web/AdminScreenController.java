package com.example.cinema_management.screen.web;

import com.example.cinema_management.screen.dto.*;
import com.example.cinema_management.screen.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/screens")
@RequiredArgsConstructor
public class AdminScreenController {

    private final ScreenService svc;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<ScreenResponse> p = svc.search(q, page, size);
        model.addAttribute("page", p);
        model.addAttribute("q", q);
        return "admin/screens/list";
    }

    @GetMapping("/new")
    public String formCreate(Model model) {
        model.addAttribute("screen", ScreenCreateRequest.builder()
                .name("").code("").description("").type("2D").capacity(0)
                .active(true)
                .build());
        model.addAttribute("screenResp", null);
        return "admin/screens/form";
    }

    @PostMapping
    public String create(@ModelAttribute("screen") @Valid ScreenCreateRequest req,
                         RedirectAttributes ra) {

        var created = svc.create(req);
        ra.addFlashAttribute("saved", true);
        return "redirect:/admin/screens";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable Long id, Model model) {
        var resp = svc.get(id);
        model.addAttribute("screenResp", resp);
        model.addAttribute("screen", ScreenUpdateRequest.builder()
                .name(resp.name()).code(resp.code()).description(resp.description()).type(resp.type()).capacity(resp.capacity())
                .active(resp.active())
                .build());
        return "admin/screens/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("screen") @Valid ScreenUpdateRequest req,
                         RedirectAttributes ra) {

        svc.update(id, req);
        ra.addFlashAttribute("saved", true);
        return "redirect:/admin/screens";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        svc.delete(id);
        return "redirect:/admin/screens";
    }


}
