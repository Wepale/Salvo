console.log("IM here Login");
const vmLog = new Vue({
    el: "#vueLogin",
    data: {},
    methods: {

        getElement(id) {
            return document.getElementById(id)
        },

        async login() {
            try {
                let response = await fetch('http://localhost:8080/api/login', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `username=${this.getElement("email").value}&password=${this.getElement("password").value}`
                });
                console.log(response);
                if (response.status === 200) {
                    window.location.reload();
                } else if (response.status === 401) {
                    alert("Sorry, wrong username or password")
                } else {
                    alert("Something went wrong, please try later")
                }
            } catch (error) {
                console.log("Error: ", error)
            }
        },

        async logout() {
            try {
                await fetch('http://localhost:8080/api/logout', {
                    method: 'POST'
                });
                window.location.reload();
            } catch (error) {
                console.log("Error: ", error)
            }
        },

        async signUp() {
            try {
                let response = await fetch('http://localhost:8080/api/players', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        userName: this.getElement("email").value,
                        password: this.getElement("password").value
                    })
                });
                if (response.status === 411) {
                    alert(`${response.status}: Some fields are empty`)
                } else if (response.status === 403) {
                    alert(`${response.status}: Sorry, name already in use`)
                } else if (response.status === 201) {
                    this.login();
                }
            } catch (error) {
                console.log("Error: ", error)
            }
        }
    }
});