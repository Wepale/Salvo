const vm = new Vue({
    el: "#vueGameView",
    data: {
        gameInfo: [],
        tableLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        gameViewURL: "http://localhost:8080/api/game_view/",
        urlParams: new URLSearchParams(window.location.search).get("gp"),
        ships: [],
        check: "",
        shipsPosition: [],
        shipIsPlaced: {
            carrier: false,
            battleship: false,
            submarine: false,
            destroyer: false,
            patrolBoat: false
        },

    },
    methods: {
        async getData(url) {
            try {
                let data = await (await fetch(url, {
                    method: "GET",
                    dataType: 'json',
                })).json();
                vm.data = data[0];
                vm.printShips(this.data);
                vm.printSalvoes(this.data);
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
            document.querySelectorAll(".selectable")
                .forEach(td => ["mouseenter", "mouseleave"]
                    .forEach(even => td.addEventListener(even, this)));
        },

        placeShip(){
            document.querySelectorAll(".selectable")
                .forEach(td => td.addEventListener("click", this));
        },

        makeShipCells2(td, shipSize, direction = "horizontal", type) {
            const letterID = td.id.slice(10,11);
            const numberID = td.id.slice(11);
            let cells = [];

            direction === "horizontal"
                ? cells = this.getHorizontalCells(numberID, letterID, shipSize)
                : cells = this.getVerticalCells(numberID, letterID, shipSize);

            if (!cells.some(cell => this.shipsPosition.includes(cell))) {
                document.querySelectorAll(".selectable")
                    .forEach(td => ["mouseenter", "mouseleave", "click"]
                        .forEach(even => td.removeEventListener(even, this)));

                cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.add(`${type}`));
                this.shipIsPlaced[type] = true;
                this.shipsPosition = [...this.shipsPosition, ...cells];
                this.ships.push({type: `${type}`, locations: cells});
            }
        },

        makeHoverCells2(td, shipSize, type, direction = "horizontal", remove = false) {
            const letterID = td.id.slice(10,11);
            const numberID = td.id.slice(11);
            let cells = [];

            direction === "horizontal"
                ? cells = this.getHorizontalCells(numberID, letterID, shipSize)
                : cells = this.getVerticalCells(numberID, letterID, shipSize);

            if (!remove) {
                cells.some(cell => this.shipsPosition.includes(cell))
                    ? cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.add("notPossibleShip"))
                    : cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.add(`${type}`));
            } else {
                cells.some(cell => this.shipsPosition.includes(cell))
                    ? cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.remove("notPossibleShip"))
                    : cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.remove(`${type}`));
            }

        },

        getHorizontalCells(numberID, letterID, shipSize){
            let numbers = [];

            11 - numberID < shipSize
                ? numbers = Array.from(Array(10), (e, i) => i + 1).slice(-shipSize)
                : numbers = Array.from(Array(10), (e, i) => i + 1).slice(numberID - 1, numberID - 1 + shipSize);

            return numbers.map(number => `${letterID}${number}`);
        },

        getVerticalCells(numberID, letterID, shipSize){
            const lettersAndNumbers = new Map(this.tableLetters.map( (letter, index) => [letter , index + 1]));
            let letters = [];

            11 - lettersAndNumbers.get(letterID) < shipSize
                ? letters = this.tableLetters.slice(-shipSize)
                : letters = this.tableLetters.slice(lettersAndNumbers.get(letterID) - 1, lettersAndNumbers.get(letterID) - 1 + shipSize);

            return letters.map(letter => `${letter}${numberID}`);
        },


        handleEvent(e) {
            const td = e.target;
            const size = Number(td.getAttribute("data-size"));
            const direction = td.getAttribute("data-direction") === "H" ? "horizontal" : "vertical";
            const type = td.getAttribute("data-type");

            switch (event.type) {
                case "click":
                    this.makeShipCells2(td, size, direction, type);
                    break;
                case "mouseenter":
                    this.makeHoverCells2(td, size, type, direction);
                    break;
                case "mouseleave":
                    this.makeHoverCells2(td, size, type, direction, true);
                    break;
                default:
                    return;
            }
        },

        addEvents(){
            this.makeHoverTable();
            this.placeShip();
        }
    },
    created() {
        this.getData(`${this.gameViewURL}${this.urlParams}`);
    },
});

