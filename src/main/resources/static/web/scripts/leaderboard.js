console.log("Im here leaderboard");
const vm = new Vue({
    el: "#vueLeaders",
    data: {
        data: [],
        playerScore: []
    },
    methods: {
        async getData() {
            try {
                let data = await (await fetch("http://localhost:8080/api/games", {
                    method: "GET",
                    dataType: 'json',
                })).json();
                console.log(data)
                this.data = data.games;
                this.playerScore = this.createObject(this.data);
                console.log(this.playerScore)
            } catch (error) {
                console.log(`Failed: ${error}`);
            }
        },
        getElement(id) {
            return document.getElementById(id)
        },

        createObject(data){
            return [...new Set(data.flatMap(game => game.gamePlayers.map(gp => gp.player.id)))]
                .map(id => {
                    return {
                        name: this.getNamePlayer(id),
                        totalScore: this.getTotalScore(id),
                        wonGames: this.getGamesByResult(id, 1),
                        lostGames: this.getGamesByResult(id, 0),
                        tiedGames: this.getGamesByResult(id, 0.5)
                    }
                })
                .slice()
                .sort((a, b) => b.totalScore - a.totalScore);
        },

        getTotalScore(playerID){
            const scores = this.data.flatMap(game => game.gamePlayers
                .filter(gp => gp.player.id === playerID && gp.score !== null))
                .map(gp => gp.score.score);
            return scores.length > 0
                ? scores.reduce((acu, score) => acu + score)
                : 0
        },

        getGamesByResult(playerID, result){
            return this.data.flatMap(game => game.gamePlayers
                .filter(gp => gp.player.id === playerID && gp.score !== null && gp.score.score === result))
                .length;
        },

        getNamePlayer(playerID){
            return this.data.flatMap(game => game.gamePlayers)
                .find(gp => gp.player.id === playerID)
                .player
                .name;
        },
    },
    created(){
        this.getData();
    }
});