document.addEventListener("DOMContentLoaded", () => {
    const switches = document.querySelectorAll(".btn3_container");

    switches.forEach(switchContainer => {
        const userId = switchContainer.id.split('-')[1];
        const mainSwitch = document.getElementById(`switchIcon-${userId}`);
        const activeCheckbox = document.getElementById(`active-${userId}`);

        if (activeCheckbox.checked) {
            mainSwitch.classList.add('active3');
        }

        switchContainer.addEventListener('click', () => {
            mainSwitch.classList.toggle('active3');
            activeCheckbox.checked = !activeCheckbox.checked;
            // Optional: Send Ajax request to update status in the backend
        });
    });
});
