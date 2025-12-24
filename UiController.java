package com.oru.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.*;
import com.oru.modelExtract.EntryPoint;

@Controller
public class UiController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/run")
    public String run(@RequestParam Map<String, String> params, Model model) {
        System.setProperty("db.url", params.get("url"));
        System.setProperty("db.user", params.get("user"));
        System.setProperty("db.pass", params.get("pass"));
        try {
            EntryPoint.main(new String[]{});
            model.addAttribute("msg", "Execution started");
        } catch (Exception e) {
            model.addAttribute("msg", "Error: " + e.getMessage());
        }
        return "index";
    }

    /**
     * GET API to fetch circuit data based on duration
     * Example: /getCircuitData?duration=30
     */
    @GetMapping("/getCircuitData")
    @ResponseBody
    public List<Map<String, String>> getCircuitData(
            @RequestParam("duration") int duration) {

        List<Map<String, String>> result = new ArrayList<>();

        // Sample data (around 20 rows)
        for (int i = 1; i <= 20; i++) {
            Map<String, String> row = new HashMap<>();
            row.put("fid", "FID-" + (1000 + i));
            row.put("circuit", "CIRCUIT-" + duration + "-" + i);
            result.add(row);
        }

        return result;
    }
}
