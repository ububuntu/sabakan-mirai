document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".check-button");

    buttons.forEach(button => {
        button.addEventListener("click", function () {
            this.disabled = true;
            this.textContent = "添削中...";
        });
    });
});