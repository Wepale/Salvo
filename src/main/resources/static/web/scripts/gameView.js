const vm = new Vue({
    el: "#vueGameView",
    data: {
        gameInfo: [],
        tableLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        gameViewURL: "http://localhost:8080/api/game_view/",
        urlParams: new URLSearchParams(window.location.search).get("gp"),
        ships: [],
        data: [],
    },
    methods: {
        async getData(url) {
            try {
                let data = await (await fetch(url, {
                    method: "GET",
                    dataType: 'json',
                })).json();
                console.log(data);
                this.data = data[0];
                this.printShips(this.data);
                this.printSalvoes(this.data);
            } catch (error) {
                console.log(`Failed: ${error}`);
            }
        },
        getElement(id) {
            return document.getElementById(id)
        },
        printShips(gameView) {
            this.ships = gameView.ships.flatMap(ship => ship.locations);
            this.ships.forEach(location => this.getElement(`mainPlayer${location}`).classList.add("table-primary"))
        },

        printSalvoes(gameView) {
            gameView.salvoes
                .filter(salvo => salvo.playerId !== gameView.gamePlayers.find(gp => gp.id === Number(this.urlParams)).player.id)
                .forEach(salvo => salvo.locations
                    .forEach(location => {
                        if (this.ships.includes(location)) {
                            this.getElement(`mainPlayer${location}`).classList.add("table-warning");
                            this.getElement(`mainPlayer${location}`).innerHTML = salvo.turn;
                        }
                    }));
        },
    },
    created() {
        this.getData(`${this.gameViewURL}${this.urlParams}`);
    }

});

