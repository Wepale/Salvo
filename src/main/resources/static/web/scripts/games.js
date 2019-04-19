const vmGames = new Vue({
    el: "#vueGames",
    data: {
        games: [],
        playerID: null
    },

    methods: {

        async getGames(){
            try {
                const data = await (await fetch("http://localhost:8080/api/games", {
                    method: "GET",
                    dataType: 'json',
                })).json();
                console.log(data);
                this.playerID = data.player ? data.player.id : null;
                this.games = data.games;
            } catch (error) {
                console.log(`Failed: ${error}`);
            }
        },

        isLoginAndGameOwner(game){
          return !!game.gamePlayers
              .filter(gp => gp.player.id === this.playerID)
              .length
        },

        goToGame(game){
            const gpID = game.gamePlayers
                .find(gp => gp.player.id === this.playerID)
                .id;
            window.location.href = `http://localhost:8080/web/game.html?gp=${gpID}`;
        },

        async createGame(){
            const date = new Date();
            try {
                let response = await fetch('http://localhost:8080/api/games', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        date: `${date.getDate() < 10 ? '0' : ''}${date.getDate()}/${date.getMonth() < 9 ? '0' : ''}${date.getMonth() + 1}/${date.getFullYear()}
                        ${date.getHours() < 10 ? '0' : ''}${date.getHours()}:${date.getMinutes() < 10 ? '0' : ''}${date.getMinutes()}:${date.getSeconds() < 10 ? '0' : ''}${date.getSeconds()}`
                    })
                });
                const message = await response.json();
                if (response.status === 201) {
                    window.location.href = `http://localhost:8080/web/game.html?gp=${message.gpID}`;
                } else if (response.status === 403) {
                    alert(`${response.status}: ${message.error}`)
                } else{
                    alert("Something went wrong, try again later");
                }
            } catch (error) {
                console.log("Error: ", error)
            }
        }
    },

    created(){
        this.getGames();
    }
});