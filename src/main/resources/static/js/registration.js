document.addEventListener("DOMContentLoaded", () => {
    const accountType = document.getElementById("accountType");
    const studentFields = document.querySelectorAll(".student-only");
    const clubFields = document.querySelectorAll(".club-only");
    const year = document.getElementById("yearOfStudy");
    const roll = document.getElementById("rollNumber");
    const club = document.getElementById("clubName");

    function updateFields() {
        const isStudent = accountType.value === "STUDENT";
        const isClub = accountType.value === "CLUB_REPRESENTATIVE";
        studentFields.forEach(field => { field.hidden = !isStudent; });
        clubFields.forEach(field => { field.hidden = !isClub; });
        year.required = isStudent;
        roll.required = isStudent;
        club.required = isClub;
    }

    accountType.addEventListener("change", updateFields);
    updateFields();
});
