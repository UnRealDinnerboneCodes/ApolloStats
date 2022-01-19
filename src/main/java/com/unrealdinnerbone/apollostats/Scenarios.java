package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class Scenarios
{
    private static final String SCENARIOS_URL = System.getenv().getOrDefault("SCENARIOS_URL", "https://pastebin.com/raw/PixjeKaS");
    private static final Logger LOGGER = LoggerFactory.getLogger(Scenarios.class);

    private static final Map<String, String> officialScenarios = new HashMap<>();
    private static final List<String> unknownScenarios = new ArrayList<>();
    private static final HashMap<String, String> scenariosRemaps = new HashMap<>();


    public static void updateOfficialScenarios() throws IOException, InterruptedException {
        LOGGER.info("Updating official scenarios...");
        officialScenarios.clear();
        String[] lines = HttpUtils.get(SCENARIOS_URL).body().split("\n");
        Arrays.stream(lines)
                .filter(Predicate.not(line -> line.contains("ApolloUHC Scenario List")))
                .filter(Predicate.not(Util::isNewLine))
                .map(line -> line.split(":", 2))
                .map(value -> value[0])
                .forEach(type -> officialScenarios.put(Util.formalize(type), type));
        officialScenarios.put("rush", "Rush");
        officialScenarios.put("custom00", "Custom 00");
        officialScenarios.put("farmgang", "Farm Gang");
        officialScenarios.put("permanentinvisibility", "Permanent Invisibility");



        officialScenarios.remove("secretteams");
        officialScenarios.remove("doubledates");
        officialScenarios.remove("mysteryteams");
        officialScenarios.remove("mysteryscenarios");
        officialScenarios.remove("loveatfirstadvancement");
        officialScenarios.remove("drafters");
        officialScenarios.remove("bloodmarket");
        officialScenarios.remove("loveatfirstlake");
        officialScenarios.remove("loveatfirstsight");

        LOGGER.info("Loaded {} official scenarios", officialScenarios.size());


    }

    public static void loadDiskData() throws Exception {
        LOGGER.info("Loading disk data...");
        unknownScenarios.clear();
        String s = Stats.getResourceAsString("scens.json");
        unknownScenarios.addAll(Arrays.asList(Arrays.stream(Util.parser().parse(String[].class, s)).map(Util::formalize).toArray(String[]::new)));

        unknownScenarios.add(Util.formalize("Secret Teams"));
        unknownScenarios.add(Util.formalize("Double Dates"));
        unknownScenarios.add(Util.formalize("Mystery Teams"));
        unknownScenarios.add(Util.formalize("Mystery Scenarios"));
        unknownScenarios.add(Util.formalize("Love At First Advancement"));
        unknownScenarios.add(Util.formalize("Drafters"));
        unknownScenarios.add(Util.formalize("Blood Market"));
        unknownScenarios.add(Util.formalize("Love at First Lake"));
        unknownScenarios.add(Util.formalize("Love at First Sight"));

        Map<String, List<String>> theMap = Util.parser().parse(Map.class, Stats.getResourceAsString("scenfixes.json"));
        theMap.forEach((key, value) -> value.forEach(s1 -> scenariosRemaps.put(s1, key)));
        LOGGER.info("Loaded {} unknown scenarios", unknownScenarios.size());
        LOGGER.info("Loaded {} remaps", scenariosRemaps.size());
    }

    private static  final List<String> prinrted = new ArrayList<>();
    public static List<String> fixScenarios(List<String> fixed) {

        List<String> newScens = new ArrayList<>();
        for(String scenario : fixed) {
            String newS = scenario;
            for(Map.Entry<String, String> stringStringEntry : scenariosRemaps.entrySet()) {
                if(scenario.equalsIgnoreCase(stringStringEntry.getKey())) {
                    newS = stringStringEntry.getValue();
                    break;
                }
            }
            String replace = Util.formalize(newS);
            if(officialScenarios.containsKey(replace)) {
                newScens.add(officialScenarios.get(replace));
            }else {
                if(!unknownScenarios.contains(replace)) {
                    if(!prinrted.contains(replace)) {
                        prinrted.add(replace);
                        LOGGER.error("Scenario not found: {} - {}", newS, replace);
                    }
                    newScens.add(replace);
                }
            }
        }
        return newScens;

    }

}
