/**
 * BloodLink - Basic Client-Side JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Phone number input: restrict to digits only and maximum 10 digits
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function (e) {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10);
        });
    }

    // 2. Age input: restrict between 18 and 65
    const ageInput = document.getElementById('age');
    if (ageInput) {
        ageInput.addEventListener('change', function () {
            const val = parseInt(this.value, 10);
            if (val < 18) this.value = 18;
            if (val > 65) this.value = 65;
        });
    }

    // 3. Confirm donation submission
    const recordDonationForm = document.getElementById('donationForm');
    if (recordDonationForm) {
        recordDonationForm.addEventListener('submit', function (e) {
            const donorSelect = document.getElementById('donorSelect');
            const donorName = donorSelect && donorSelect.options[donorSelect.selectedIndex] 
                ? donorSelect.options[donorSelect.selectedIndex].text 
                : 'selected donor';

            const confirmed = confirm('Are you sure you want to record this donation for ' + donorName + '?\nThis will place the donor in the 90-day cooldown period.');
            if (!confirmed) {
                e.preventDefault();
            }
        });
    }
});
