package com.example.cinema_management.schedule.web;

import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.pricing.PricingRepository;
import com.example.cinema_management.schedule.dto.ScheduleCreateRequest;
import com.example.cinema_management.schedule.dto.ScheduleUpdateRequest;
import com.example.cinema_management.schedule.service.ScheduleService;
import com.example.cinema_management.screen.repository.ScreenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final ScheduleService svc;
    private final MovieRepository movieRepo;
    private final PricingRepository pricingRepo;
    private final ScreenRepository screenRepo;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var pageable = PageRequest.of(page, size, Sort.by("sessionStartTime").descending());
        var result = svc.list(q == null ? null : q.trim(), pageable);
        model.addAttribute("page", result);
        model.addAttribute("q", q == null ? "" : q);
        return "admin/schedules/list";
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        model.addAttribute("resp", null);
        model.addAttribute("movies", movieRepo.findAll());
        model.addAttribute("prices", pricingRepo.findAll());
        model.addAttribute("screens", screenRepo.findAll());
        return "admin/schedules/form";
    }

    @PostMapping
    public String create(@ModelAttribute @Valid ScheduleCreateRequest req,
                         RedirectAttributes ra) {
        try {
            svc.create(req);
            ra.addFlashAttribute("saved", true);
            return "redirect:/admin/schedules";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/schedules/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable Long id, Model model) {
        var resp = svc.get(id);
        model.addAttribute("resp", resp);
        model.addAttribute("movies", movieRepo.findAll());
        model.addAttribute("prices", pricingRepo.findAll());
        model.addAttribute("screens", screenRepo.findAll());
        return "admin/schedules/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute @Valid ScheduleUpdateRequest req,
                         RedirectAttributes ra) {
        try {
            svc.update(req);
            ra.addFlashAttribute("saved", true);
            return "redirect:/admin/schedules";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/schedules/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        svc.delete(id);
        ra.addFlashAttribute("deleted", true);
        return "redirect:/admin/schedules";
    }
}
