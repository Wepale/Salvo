const vm = new Vue({
    el: "#vueGameView",
    data: {
        gameInfo: [],
        tableLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        gameViewURL: "http://localhost:8080/api/game_view/",
        urlParams: new URLSearchParams(window.location.search).get("gp"),
        shipsToSend: [],
        check: "",
        shipsPosition: [],
        shipIsPlaced: {
            carrier: false,
            battleship: false,
            submarine: false,
            destroyer: false,
            patrolBoat: false
        },
        salvosLocations: [],
        newSalvo: [],
        turn: 0,
        hitsOnMe: [],
        hitsOnEnemy: [],
        intervalID: "",
        allCorrect: true
    },
    methods: {
        async getData(url) {
            try {
                const response = await fetch(url, {
                    method: "GET",
                    dataType: 'json',
                });
                const data = await response.json();
                console.log(data[0]);
                if (response.status === 200) {
                    this.makeInterval();
                    console.log("IM executin every 3 seconds");
                    this.gameInfo = data[0];
                    this.checkShips(data[0]);
                    this.printShips(data[0]);
                    this.printSalvoes(data[0]);
                    this.turn = this.getHighestTurn(data[0]);
                    this.hitsOnMe = this.getHitsOnShips(this.gameInfo, "hitsOnMe");
                    this.hitsOnEnemy = this.getHitsOnShips(this.gameInfo, "hitsOnEnemy");
                    this.makeGameLogic(this.gameInfo);
                } else if (response.status === 403) {
                    alert (`Error ${response.status}: ${data[0].error}`)
                } else if (response.status === 401) {
                    alert ('You must be logged')
                } else {
                    alert ('Something went wrong, try again latter');
                }
            } catch (error) {
                console.log(`Failed: ${error}`);
            }
        },

        async postShips(){
            try {
                const response = await (fetch(`http://localhost:8080/api/games/players/${this.urlParams}/ships`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(this.shipsToSend)
                }));
                const message = await response.json();
                if (response.status === 201) {
                    this.getData(`${this.gameViewURL}${this.urlParams}`);
                    this.shipsToSend = [];
                } else if (response.status === 403 || response.status === 401) {
                    alert(`${response.status}: ${message.error}`);
                    this.removeShipsFromTable();
                } else {
                    alert("Something went wrong, try again later");
                }
            } catch (e) {
                console.log("Error: ", e );
            }
        },

        async postSalvo(){
            try {
                const response = await (fetch(`http://localhost:8080/api/games/players/${this.urlParams}/salvoes`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({turn: this.turn, locations: this.newSalvo})
                }));
                console.log(response);
                const message = await response.json();
                if (response.status === 201) {
                    this.newSalvo = [];
                    this.getData(`${this.gameViewURL}${this.urlParams}`);
                } else if (response.status === 403 || response.status === 401) {
                    alert(`${response.status}: ${message.error}`);
                    this.removeClassesFromTable();
                } else {
                    alert("Something went wrong, try again later");
                }
            } catch (e) {
                console.log("Error: ", e );
            }
        },

        getElement(id) {
            return document.getElementById(id)
        },

        removeClassesFromTable(){
            document.querySelectorAll(".possibleSalvo, .possibleShip")
                .forEach(td => td.classList.remove("possibleSalvo", "possibleShip"))
        },

        removeShipsFromTable() {
            Object.keys(this.shipIsPlaced).forEach(ship => document.querySelectorAll(`.${ship}`)
                .forEach(td => td.classList.remove(ship)));
        },

        printShips(gameData) {
            gameData.ships
                .forEach(ship => ship.locations
                    .forEach(location => this.getElement(`mainPlayer${location}`).classList.add(`${ship.type}`)));
        },

        addClassAndTurn(id, myClass, salvo) {
            this.getElement(id).classList.add(myClass);
            this.getElement(id).innerHTML = salvo.turn;
        },

        printSalvoes(gameData) {
            gameData.salvoes
                .forEach(salvo => salvo.locations
                    .forEach(location => {
                        if (salvo.playerId !== gameData.gamePlayers.find(gp => gp.id === Number(this.urlParams)).player.id) {

                            gameData.ships.flatMap(ship => ship.locations).includes(location)
                                ? this.addClassAndTurn(`mainPlayer${location}`, "hit", salvo)
                                : this.addClassAndTurn(`mainPlayer${location}`, "noHit", salvo);

                        } else if (gameData.hitsOnEnemy
                            .flatMap(hit => hit.ships
                                .flatMap(ship => ship.hitLocations))
                            .includes(location)) {

                            this.addClassAndTurn(`salvo${location}`, "hit", salvo);
                            this.salvosLocations.push(location);

                        } else {
                            this.addClassAndTurn(`salvo${location}`, "noHit", salvo);
                        }
                    }));
        },

        makeSalvoTableHover() {
            document.querySelectorAll(".salvoTable")
                .forEach(td => ["mouseenter", "mouseleave"]
                    .forEach(even => td.addEventListener(even, this)));
        },

        hoverCellsOnSalvoTable(td, remove = false) {

            if (remove) {
                this.salvosLocations.includes(td.id.slice(5))
                    ? td.classList.remove("notPossibleSalvo")
                    : td.classList.remove("possibleSalvo")
            } else {
                this.salvosLocations.includes(td.id.slice(5))
                    ? td.classList.add("notPossibleSalvo")
                    : td.classList.add("possibleSalvo")
            }
        },

        placeSalvo(){
            document.querySelectorAll(".salvoTable")
                .forEach(td => td.addEventListener("click", this));
        },

        makeSalvoCell(td) {
            if (!this.salvosLocations.includes(td.id.slice(5))) {
                document.querySelectorAll(".salvoTable")
                    .forEach(td => ["mouseenter", "mouseleave", "click"]
                        .forEach(even => td.removeEventListener(even, this)));
                td.classList.add("possibleSalvo");
                this.salvosLocations.push(td.id.slice(5));
                this.newSalvo.push(td.id.slice(5));
                console.log(this.newSalvo);
                if (this.newSalvo.length < 5) {
                    this.addEventsOnSalvoTable();
                }
            }
        },

        getHighestTurn(gameData) {
            const turns = gameData.salvoes
                .filter(salvo => salvo.locations
                    .some(location => salvo.playerId === gameData.gamePlayers.find(gp => gp.id === Number(this.urlParams)).player.id))
                .map(salvo => salvo.turn);

            return turns.length
                ? Math.max(...turns) + 1
                : 1
        },

        highestTurnThanOpponent(gameData) {
            const myTurn = this.getHighestTurn(gameData) - 1;
            const enemyGP = gameData.gamePlayers.find(gp => gp.id !== Number(this.urlParams));
            const turns = enemyGP
                ? gameData.salvoes
                .filter(salvo => salvo.locations
                    .some(location => salvo.playerId === enemyGP.player.id))
                    .map(salvo => salvo.turn)
                : [];

            const enemyTurn = turns.length ? Math.max(...turns) : 0;
            return myTurn > enemyTurn
        },

        winGame(gameData, meOrEnemy) {
            return gameData[meOrEnemy]
                .flatMap(hit => hit.ships)
                .flatMap(ship => ship.hitLocations)
                .length === 17;
        },

        gameIsFinish(gameData) {
            return this.winGame(gameData, "hitsOnMe") || this.winGame(gameData, "hitsOnEnemy");
        },

        getHitsOnShips(gameData, meOrEnemy) {
            return ["carrier", "battleship", "submarine", "destroyer", "patrolBoat"].map(shipType => {
                return {
                    type: shipType,
                    hitLocations: gameData[meOrEnemy]
                        .flatMap(hit => hit.ships
                            .filter(ship => ship.type === shipType))
                        .flatMap(ship => ship.hitLocations)
                }
            });
        },

        makeGameLogic(gameData) {
            if (!this.highestTurnThanOpponent(gameData) && !this.gameIsFinish(gameData) && gameData.gamePlayers.length === 2) {
                this.addEventsOnSalvoTable();
            }

            if(this.gameIsFinish(gameData)) {
                clearInterval(this.intervalID);
            }
        },

        makeInterval() {
            if (this.allCorrect) {
                this.intervalID = window.setInterval(() => this.getData(`${this.gameViewURL}${this.urlParams}`), 3000);
                this.allCorrect = false;
            }
        },

        checkShips(gameData) {
            if (gameData.ships.length !== 0) {
                Object.keys(this.shipIsPlaced)
                    .forEach(ship => this.shipIsPlaced[ship] = true);
            }
        },

        makeShipTableHover(){
            document.querySelectorAll(".shipTable")
                .forEach(td => ["mouseenter", "mouseleave"]
                    .forEach(even => td.addEventListener(even, this)));
        },

        placeShip(){
            document.querySelectorAll(".shipTable")
                .forEach(td => td.addEventListener("click", this));
        },

        makeShipCells(td, shipSize, direction = "horizontal", type) {
            const letterID = td.id.slice(10,11);
            const numberID = td.id.slice(11);
            let cells = [];

            direction === "horizontal"
                ? cells = this.getHorizontalCells(numberID, letterID, shipSize)
                : cells = this.getVerticalCells(numberID, letterID, shipSize);

            if (!cells.some(cell => this.shipsPosition.includes(cell))) {
                document.querySelectorAll(".shipTable")
                    .forEach(td => ["mouseenter", "mouseleave", "click"]
                        .forEach(even => td.removeEventListener(even, this)));

                cells.forEach(cell => this.getElement(`mainPlayer${cell}`).classList.add(`${type}`));
                this.shipIsPlaced[type] = true;
                this.shipsPosition.push(...cells);
                console.log(type, cells);
                this.shipsToSend.push({type: `${type}`, location: cells});
            }
        },

        hoverCellsOnShipTable(td, shipSize, type, direction = "horizontal", remove = false) {
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
                ? numbers = Array.from(Array(10), (k, i) => i + 1).slice(-shipSize)
                : numbers = Array.from(Array(10), (k, i) => i + 1).slice(numberID - 1, numberID - 1 + shipSize);

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
                    td.id.startsWith("mainPlayer")
                        ? this.makeShipCells(td, size, direction, type)
                        : this.makeSalvoCell(td);
                    break;
                case "mouseenter":
                    td.id.startsWith("mainPlayer")
                        ? this.hoverCellsOnShipTable(td, size, type, direction)
                        : this.hoverCellsOnSalvoTable(td);
                    break;
                case "mouseleave":
                    td.id.startsWith("mainPlayer")
                        ? this.hoverCellsOnShipTable(td, size, type, direction, true)
                        : this.hoverCellsOnSalvoTable(td, true);
                    break;
                default:
                    return;
            }
        },

        addEventsOnShipTable() {
            this.makeShipTableHover();
            this.placeShip();
        },

        addEventsOnSalvoTable() {
            this.makeSalvoTableHover();
            this.placeSalvo();
        }
    },
    created() {
        this.getData(`${this.gameViewURL}${this.urlParams}`);
    },
});

