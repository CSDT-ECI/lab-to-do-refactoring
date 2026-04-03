const calculateBtn = document.getElementById("calculateBtn");
const darkModeBtn = document.getElementById("darkModeBtn");
const errorMessage = document.getElementById("errorMessage");
const result = document.getElementById("result");

const getNumber = (id) => {
  const value = document.getElementById(id).value;
  return value === "" ? null : Number(value);
};

const isValidNumber = (value) => Number.isFinite(value);

const calculate = () => {
  const carKm = getNumber("carKm");
  const flightHours = getNumber("flightHours");
  const electricitySpend = getNumber("electricitySpend");

  if (carKm === null || flightHours === null || electricitySpend === null) {
    errorMessage.textContent = "Completa los tres campos antes de calcular.";
    return;
  }

  if (!isValidNumber(carKm) || !isValidNumber(flightHours) || !isValidNumber(electricitySpend)) {
    errorMessage.textContent = "Usa solo numeros validos en los tres campos.";
    return;
  }

  errorMessage.textContent = "";
  const total = (carKm + flightHours + electricitySpend) * 0.5;
  result.textContent = `Total de Huella de Carbono en KG: ${total}`;
};

calculateBtn.addEventListener("click", calculate);
darkModeBtn.addEventListener("click", () => {
  document.body.classList.add("dark-mode");
});
