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

        makeHoverTable(){
            document.querySelectorAll(".grey")
                .forEach(td => ["mouseenter", "mouseleave"]
                        .forEach(even => td.addEventListener(even, () => this.makeHoverCells(td, 4, "vertical")))
                )
        },

        makeHoverHorizontal(numberID, letterID, shipSize){
            let numbers = [];

            11 - numberID < shipSize
                ? numbers = Array.from(Array(10), (e, i) => i + 1).slice(-shipSize)
                : numbers = Array.from(Array(10), (e, i) => i + 1).slice(numberID - 1, numberID - 1 + shipSize);

            numbers.forEach(number => this.getElement(`mainPlayer${letterID}${number}`).classList.toggle("ship"));
        },

        makeHoverVertical(numberID, letterID, shipSize){
            const lettersAndNumbers = new Map(this.tableLetters.map( (letter, index) => [letter , index + 1]));
            let letters = [];

            11 - lettersAndNumbers.get(letterID) < shipSize
                ? letters = this.tableLetters.slice(-shipSize)
                : letters = this.tableLetters.slice(lettersAndNumbers.get(letterID) - 1, lettersAndNumbers.get(letterID) - 1 + shipSize);

            letters.forEach(letter => this.getElement(`mainPlayer${letter}${numberID}`).classList.toggle("ship"));
        },

        makeHoverCells(td, shipSize, direcction = "horizontal") {
            const letterID = td.id.slice(10,11);
            const numberID = td.id.slice(11);

            direcction === "horizontal"
                ? this.makeHoverHorizontal(numberID, letterID, shipSize)
                : this.makeHoverVertical(numberID, letterID, shipSize);
            }
    },
    created() {
        this.getData(`${this.gameViewURL}${this.urlParams}`);
    },

    mounted(){
        this.makeHoverTable();
    }

});

