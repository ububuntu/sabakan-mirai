document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form.menu");
    const checkButtons = document.querySelectorAll(".check-button");
    // 保存ボタンを取得
    const saveButton = document.querySelector("button[value='save']");

    checkButtons.forEach(button => {
        button.addEventListener("click", function (event) {
            event.preventDefault();

            // 1. ボタンの情報をhiddenで追加
            const hiddenAction = document.createElement('input');
            hiddenAction.type = 'hidden';
            hiddenAction.name = this.name;
            hiddenAction.value = this.value;
            form.appendChild(hiddenAction);

            // 2. すべての添削ボタンを無効化
            checkButtons.forEach(btn => {
                btn.disabled = true;
                btn.style.opacity = "0.5";
            });

            // 3. 保存ボタンも無効化する
            if (saveButton) {
                saveButton.disabled = true;
                saveButton.style.opacity = "0.5";
                saveButton.style.cursor = "not-allowed";
            }

            // 4. 押されたボタンのテキストを変更
            this.textContent = "添削中...";

            // 5. フォームを送信
            form.submit();
        });
    });
});