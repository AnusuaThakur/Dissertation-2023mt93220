package com.dissertation.demo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminUIController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserDataService userDataService;

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(@RequestParam(required = false) Long userId,Model model) {
        // Get metrics from the service layer (simulated in this case)
        String metrics = adminService.getNetworkMetrics();
        String[] metricArray = metrics.split(", ");
        String load = metricArray[0].split(": ")[1].replace("%", "");
        String throughput = metricArray[1].split(": ")[1].replace(" KB/s", "");
        String latency = metricArray[2].split(": ")[1].replace(" ms", "");
        
        model.addAttribute("load", load);
        model.addAttribute("throughput", throughput);
        model.addAttribute("latency", latency);
        model.addAttribute("lastActiveUserId", userDataService.getLastActiveUserId());
        if (userId != null) {
            // Show data for a specific user
            model.addAttribute("userId", userId);
            model.addAttribute("userData", userDataService.getLastSubmittedData(userId));
            model.addAttribute("lastChangedFieldCount", userDataService.getLastChangedFieldCount(userId));
            model.addAttribute("load", userDataService.getNetworkLoadForUser(userId));
        } else {
            // Show data for all users
            Map<Long, Map<String, String>> allUserData = userDataService.getAllUserSubmittedData();
            Map<Long, String> modifiedFieldsMap = allUserData.keySet().stream()
                    .collect(Collectors.toMap(user -> user, user -> userDataService.getModifiedFields(user)));
            model.addAttribute("allUserData", allUserData);
            model.addAttribute("lastChangedFieldCount", userDataService.getAllChangedFieldCounts());
            model.addAttribute("modifiedFieldsMap", modifiedFieldsMap);
            Map<Long, Integer> userChangeCounts = userDataService.getAllUserSubmittedData().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        entry -> userDataService.getLastChangedFieldCount(entry.getKey())
                    ));

            // Sort by change count in descending order and get the top 5 users
            Map<Long, Integer> topUsers = userChangeCounts.entrySet().stream()
                    .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                    .limit(5)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue, 
                        (e1, e2) -> e1, 
                        LinkedHashMap::new
                    ));

            model.addAttribute("topUsers", topUsers); // Add this to the model
        }
        return "adminDashboard";
    }
}

