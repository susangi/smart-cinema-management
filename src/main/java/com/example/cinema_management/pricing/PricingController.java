package com.example.cinema_management.pricing;

import com.example.cinema_management.pricing.dto.PricingCreateRequest;
import com.example.cinema_management.pricing.dto.PricingForm;
import com.example.cinema_management.pricing.dto.PricingUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

/**
 *
 * Manages CRUD operations for pricing definitions in the Admin Portal.
 * Exposes list/create/edit/delete endpoints and resolves seat-type prices
 * via {@link PricingType} records. Includes per-controller exception handling
 * that converts server errors into user-friendly messages with proper logging.
 */
@Controller
@RequestMapping("/admin/pricing")
public class PricingController {

    private static final Logger log = LoggerFactory.getLogger(PricingController.class);

    private final PricingService service;
    private final PricingRepository pricingRepository;
    private final PricingTypeRepository pricingTypeRepository;

    public PricingController(PricingService service,
                             PricingRepository pricingRepository1,
                             PricingTypeRepository pricingTypeRepository) {
        this.service = service;
        this.pricingRepository = pricingRepository1;
        this.pricingTypeRepository = pricingTypeRepository;
    }

    /**
     * Lists existing pricing rows with a simple page/size pagination.
     *
     * @param page  current page index (0-based), defaults to 0
     * @param size  page size, defaults to 5
     * @param model thymeleaf model
     * @return view name for pricing list
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       Model model) {

        Page<Pricing> pg = pricingRepository.findAll(PageRequest.of(page, size));

        Map<Long, Map<SeatType, BigDecimal>> priceMap = new HashMap<>();
        for (Pricing pricing : pg.getContent()) {
            List<PricingType> types = pricingTypeRepository.findByPricingId(pricing.getId());
            Map<SeatType, BigDecimal> m = new EnumMap<>(SeatType.class);
            for (PricingType pt : types) {
                m.put(pt.getType(), pt.getPrice());
            }
            priceMap.put(pricing.getId(), m);
        }

        model.addAttribute("page", pg);
        model.addAttribute("priceMap", priceMap);
        model.addAttribute("pageTitle", "Pricing");

        return "admin/pricing/pricing-list";
    }

    /**
     * Renders empty form for creating a new pricing.
     *
     * @param model thymeleaf model
     * @return view name for pricing form (create mode)
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        var form = new PricingForm();
        form.setName("");
        form.setAdultPrice(new BigDecimal("0.00"));
        form.setChildPrice(new BigDecimal("0.00"));

        model.addAttribute("pageTitle", "Create Pricing");
        model.addAttribute("form", form);
        model.addAttribute("mode", "create");

        return "admin/pricing/pricing-form";
    }

    /**
     * Handles submission of a new pricing definition.
     *
     * @param form     validated pricing form
     * @param binding  binding/validation result
     * @param ra       redirect flash attrs
     * @param model    thymeleaf model
     * @return redirect to list on success or back to form on validation error
     */
    @PostMapping
    public String createSubmit(@Valid @ModelAttribute("form") PricingForm form,
                               BindingResult binding,
                               RedirectAttributes ra,
                               Model model) {
        if (binding.hasErrors()) {
            log.debug("Create pricing validation errors: {}", binding.getAllErrors());
            model.addAttribute("pageTitle", "Create Pricing");
            model.addAttribute("mode", "create");
            return "admin/pricing/pricing-form";
        }

        var req = new PricingCreateRequest();
        req.name = form.getName();
        req.prices = new EnumMap<>(SeatType.class);
        req.prices.put(SeatType.ADULT, form.getAdultPrice());
        req.prices.put(SeatType.CHILD, form.getChildPrice());
        req.setStatus("ACTIVE".equalsIgnoreCase(form.getStatus()) ? Status.ACTIVE : Status.DEACTIVE);

        try {
            Long id = service.create(req);
            log.info("Pricing created: id={}, name={}", id, req.name);
            ra.addFlashAttribute("success", "Pricing created.");

            return "redirect:/admin/pricing";
        } catch (IllegalArgumentException ex) {


            log.warn("Create pricing failed (domain error): {}", ex.getMessage());
            binding.rejectValue("name", "name.exists", ex.getMessage());
            model.addAttribute("pageTitle", "Create Pricing");
            model.addAttribute("mode", "create");

            return "admin/pricing/pricing-form";
        }
    }

    /**
     * Renders edit form for an existing pricing.
     *
     * @param id    pricing id
     * @param model thymeleaf model
     * @return view name for pricing form (edit mode)
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var p = service.getOrThrow(id); // expected to throw if not found
        var types = service.listTypes(id);

        Map<SeatType, BigDecimal> map = new EnumMap<>(SeatType.class);
        types.forEach(t -> map.put(t.getType(), t.getPrice()));

        var form = new PricingForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setStatus(p.getStatus().name());
        form.setAdultPrice(map.getOrDefault(SeatType.ADULT, new BigDecimal("0.00")));
        form.setChildPrice(map.getOrDefault(SeatType.CHILD, new BigDecimal("0.00")));

        model.addAttribute("pageTitle", "Edit Pricing");
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");

        return "admin/pricing/pricing-form";
    }

    /**
     * Handles update submission for an existing pricing definition.
     *
     * @param id      pricing id
     * @param form    validated pricing form
     * @param binding binding/validation result
     * @param ra      redirect flash attrs
     * @param model   thymeleaf model
     * @return redirect to list on success or back to edit form on error
     */
    @PostMapping("/{id}")
    public String editSubmit(@PathVariable Long id,
                             @Valid @ModelAttribute("form") PricingForm form,
                             BindingResult binding,
                             RedirectAttributes ra,
                             Model model) {
        if (binding.hasErrors()) {
            log.debug("Edit pricing validation errors (id={}): {}", id, binding.getAllErrors());
            model.addAttribute("pageTitle", "Edit Pricing");
            model.addAttribute("mode", "edit");
            return "admin/pricing/pricing-form";
        }

        var req = new PricingUpdateRequest();
        req.name = form.getName();
        req.prices = new EnumMap<>(SeatType.class);
        req.prices.put(SeatType.ADULT, form.getAdultPrice());
        req.prices.put(SeatType.CHILD, form.getChildPrice());
        req.setStatus("ACTIVE".equalsIgnoreCase(form.getStatus()) ? Status.ACTIVE : Status.DEACTIVE);

        try {
            service.update(id, req);
            log.info("Pricing updated: id={}, name={}", id, req.name);
            ra.addFlashAttribute("success", "Pricing updated.");

            return "redirect:/admin/pricing";
        } catch (IllegalArgumentException ex) {

            log.warn("Edit pricing failed (id={}, domain error): {}", id, ex.getMessage());
            binding.rejectValue("name", "name.exists", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Pricing");
            model.addAttribute("mode", "edit");

            return "admin/pricing/pricing-form";
        }
    }

    /**
     * Deletes a pricing definition by id.
     *
     * @param id pricing id
     * @param ra redirect flash attrs
     * @return redirect to list
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        log.info("Pricing deleted: id={}", id);
        ra.addFlashAttribute("info", "Pricing deleted.");
        return "redirect:/admin/pricing";
    }
}
