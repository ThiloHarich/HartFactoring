package de.harich.thilo.factoring;

import de.harich.thilo.factoring.data.Factorisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FactoringController {

    private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("0.000000000");

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
        List<Factorisation> results = factorisationRunner.getFactorisationOutput(numbers);
        
        List<FactoringResult> resultList = new ArrayList<>();
        String[] inputNumbers = numbers.split(",");

        for (int i = 0; i < results.size(); i++) {
            Factorisation result = results.get(i);
            String input = (i < inputNumbers.length) ? inputNumbers[i].trim() : "";
            resultList.add(new FactoringResult(input, result.product, result.csvList, SECONDS_FORMAT.format(result.seconds)));
        }
        
        model.addAttribute("results", resultList);
        model.addAttribute("input", numbers);
        return "index";
    }

    @GetMapping("/api/factorize")
    @ResponseBody
    public List<FactoringResult> factorizeApi(@RequestParam String numbers) {
        List<Factorisation> results = factorisationRunner.getFactorisationOutput(numbers);
        List<FactoringResult> resultList = new ArrayList<>();
        String[] inputNumbers = numbers.split(",");
        
        for (int i = 0; i < results.size(); i++) {
            String input = (i < inputNumbers.length) ? inputNumbers[i].trim() : "";
            String seconds = SECONDS_FORMAT.format(results.get(i).seconds);
            resultList.add(new FactoringResult(input, results.get(i).product, results.get(i).csvList, seconds));
        }
        return resultList;
    }

    public static class FactoringResult {
        private String input;
        private String output;
        private String csv;
        private String seconds;

        public FactoringResult(String input, String output, String csv, String seconds) {
            this.input = input;
            this.output = output;
            this.csv = csv;
            this.seconds = seconds;
        }

        public String getInput() { return input; }
        public String getOutput() { return output; }
        public String getCsv() { return csv; }
        public String getSeconds() { return seconds; }
    }
}
