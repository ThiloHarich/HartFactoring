package de.harich.thilo.factoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FactoringController {

    @Autowired
    private FactorisationRunner factorisationRunner;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/factorize")
    public String factorize(@RequestParam String numbers, Model model) {
        return performFactorization(numbers, model);
    }

    @GetMapping("/factorize")
    public String factorizeGet(@RequestParam(required = false) String numbers, Model model) {
        if (numbers == null || numbers.isBlank()) {
            return "redirect:/";
        }
        return performFactorization(numbers, model);
    }

    private String performFactorization(String numbers, Model model) {
        String[][] results = factorisationRunner.getFactorisationOutput(numbers);
        
        List<FactoringResult> resultList = new ArrayList<>();
        String[] inputNumbers = numbers.split(",");
        
        for (int i = 0; i < results.length; i++) {
            String input = (i < inputNumbers.length) ? inputNumbers[i].trim() : "";
            if (results[i].length >= 2) {
                resultList.add(new FactoringResult(input, results[i][0], results[i][1]));
            } else {
                resultList.add(new FactoringResult(input, results[i][0], ""));
            }
        }
        
        model.addAttribute("results", resultList);
        model.addAttribute("input", numbers);
        return "index";
    }

    @GetMapping("/api/factorize")
    @ResponseBody
    public List<FactoringResult> factorizeApi(@RequestParam String numbers) {
        String[][] results = factorisationRunner.getFactorisationOutput(numbers);
        List<FactoringResult> resultList = new ArrayList<>();
        String[] inputNumbers = numbers.split(",");
        
        for (int i = 0; i < results.length; i++) {
            String input = (i < inputNumbers.length) ? inputNumbers[i].trim() : "";
            if (results[i].length >= 2) {
                resultList.add(new FactoringResult(input, results[i][0], results[i][1]));
            } else {
                resultList.add(new FactoringResult(input, results[i][0], ""));
            }
        }
        return resultList;
    }

    public static class FactoringResult {
        private String input;
        private String output;
        private String csv;

        public FactoringResult(String input, String output, String csv) {
            this.input = input;
            this.output = output;
            this.csv = csv;
        }

        public String getInput() { return input; }
        public String getOutput() { return output; }
        public String getCsv() { return csv; }
    }
}
