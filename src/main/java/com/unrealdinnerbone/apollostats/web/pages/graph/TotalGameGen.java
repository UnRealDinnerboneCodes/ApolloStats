package com.unrealdinnerbone.apollostats.web.pages.graph;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class TotalGameGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        StringBuilder builder = new StringBuilder("Time,Amount\n");
        AtomicInteger amount = new AtomicInteger();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isApolloGame)
                .filter(Predicate.not(Match::removed))
                .map(Match::opens)
                .map(Instant::parse)
                .sorted(Comparator.comparing(Instant::toEpochMilli))
                        .forEach(instant -> builder.append(instant).append(",").append(amount.incrementAndGet()).append("\n"));
        return builder.toString();
    }

    @Override
    public String getName() {
        return "total_game";
    }

    public static String getPage() {
        return """
                                
                <CTYPE html>
                    <html lang="en">
                                
                    <head>
                        <meta charset="UTF-8">
                        <title>Stats</title>
                        <!-- <link href="https://fonts.googleapis.com/css?family=Merriweather|Roboto" rel="stylesheet"> -->
                        <style>
                            body {
                                font-family: 'Roboto';
                                color: #333;
                                background-color: #F9F7EB;
                            }
                                
                            #title {
                                font-family: 'Merriweather';
                                text-align: center;
                                padding: 30px;
                                font-size: 28px;
                                letter-spacing: -0.01em;
                                color: #333;
                            }
                                
                            #legend {
                                display: flex;
                                justify-content: flex-end;
                                padding: 0 10px 10px;
                                font-size: 12px;
                            }
                                
                            #legend .item {
                                line-height: 12px;
                                padding: 0 10px 0 4px;
                            }
                                
                            #legend .item.male {
                                border-left: 12px solid #19A0AA;
                            }
                                
                            #legend .item.female {
                                border-left: 12px solid #F15F36;
                            }
                                
                                
                            #chart-wrapper {
                                height: 1000px;
                            }
                                
                            #footer {
                                display: flex;
                                justify-content: space-between;
                                padding: 20px 10px;
                                color: #aaa;
                                font-size: 12px;
                            }
                                
                            #footer a {
                                color: #aaa;
                            }
                        </style>
                        <script>
                            window.console = window.console || function (t) { };
                        </script>
                        <script>
                            if (document.location.search.match(/type=embed/gi)) {
                                window.parent.postMessage("resize", "*");
                            }
                        </script>
                    </head>
                                
                    <body translate="no">
                        <div id="title">Games Hosted</div>
                        <div id="legend">
                            <!-- <div class="item female">Women</div> -->
                            <!-- <div class="item male">Men</div> -->
                        </div>
                        <div id="chart-wrapper">
                            <canvas id="chart"></canvas>
                        </div>
                        <!-- <div id="footer">
                            // <div class="left"><a href="https://createwithdata.com/chartjs-and-csv/" target="_blank">About</a></div>
                            // <div class="right">Source: <a href="https://public.tableau.com/en-us/s/resources"
                                    target="_blank">Tableau</a> /
                                Wikipedia</div>
                        </div> -->
                        <script
                            src="https://cpwebassets.codepen.io/assets/common/stopExecutionOnTimeout-1b93190375e9ccc259df3a57c1abc0e64599724ae30d7ea4c6877eb615f89387.js"></script>
                                
                        <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js"></script>
                        <script src="https://cdnjs.cloudflare.com/ajax/libs/google-palette/1.1.0/palette.js"></script>
                        <script src='https://cdnjs.cloudflare.com/ajax/libs/d3/5.7.0/d3.min.js'></script>
                        <script src='https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.3/Chart.min.js'></script>
                  \s
                                
                        <script id="rendered-js">
                            Chart.defaults.global.defaultFontFamily = 'Roboto';
                            Chart.defaults.global.defaultFontColor = '#333';
                                
                            function getRandomColor() {
                            var letters = '0123456789ABCDEF'.split('');
                            var color = '#';
                            for (var i = 0; i < 6; i++ ) {
                                color += letters[Math.floor(Math.random() * 16)];
                            }
                            return color;
                                }
                                
                            function makeChart(players) {
                                
                                var playerLabels = players.map(function (d) { return d.Host; });
                                var weeksData = players.map(function (d) { return +d.Amount; });
                                var time = players.map(function (d) { return d.Time; });
                                
                                var dataMap = new Map();
                                
                                players.forEach(data => {
                                    if (!dataMap.has(data.Host)) {
                                        dataMap.set(data.Host, []);
                                    }
                                    dataMap.get(data.Host).push(
                                        {
                                            "t": data.Time,
                                            "y": +data.Amount
                                        }
                                    )
                                })
                                
                                
                                var theData = []
                                
                                dataMap.forEach(function(key, value) {
                                    theData.push(
                                        {
                                            "label": value,
                                            "data": key,
                                            backgroundColor: getRandomColor(),
                                            fill: false,
                                            fillColor: 'rgba(0, 0, 0, 0.0)'
                                        }
                                    )
                                })
                                
                                
                                // var playerColors = '#19A0AA'
                                console.log(theData)
                                var chart = new Chart('chart', {
                                    type: 'line',
                                    data: {
                                        labels: time,
                                        datasets: theData
                                    },
                                    options: {
                                        label: "Test",
                                        scales: {
                                            xAxes: [{
                                                type: 'time',
                                                distribution: 'linear'
                                            }]
                                        }
                                    }
                                });
                                
                                
                                
                                
                            }
                                
                            d3.csv('https://apollo.unreal.codes/data/games_hosted.csv').then(makeChart);
                    //# sourceURL=pen.js
                        </script>
                                
                                
                                
                    </body>
                                
                    </html>
                """;
    }
}
