export const DAY_LABELS = ["Mon", "", "Wed", "", "Fri", "", ""];

export function toDateStr(d) {
  const yyyy = d.getFullYear();
  const mm   = String(d.getMonth() + 1).padStart(2, "0");
  const dd   = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

export function capitalizeStatus(s) {
  return s.charAt(0) + s.slice(1).toLowerCase();
}

export function getFirstMondayOfYear(year) {
  const d = new Date(year, 0, 1);
  const dow = d.getDay();
  if (dow !== 1) d.setDate(d.getDate() + (dow === 0 ? 1 : 8 - dow));
  return d;
}

export function cellDateOf(base, wi, di) {
  const d = new Date(base);
  d.setDate(d.getDate() + wi * 7 + di);
  return d;
}

export function buildYearsData(applicationsByDate) {
  const dateMap = new Map();
  applicationsByDate.forEach(entry => dateMap.set(entry.date, entry));

  const currentYear = new Date().getFullYear();
  const prevYear    = currentYear - 1;
  const hasPrev     = applicationsByDate.some(e => e.date.startsWith(String(prevYear)));
  const availableYears = hasPrev ? [currentYear, prevYear] : [currentYear];

  const yearsData = {};
  for (const year of availableYears) {
    const base  = getFirstMondayOfYear(year);
    const weeks = [];
    let total   = 0;

    for (let wi = 0; wi < 54; wi++) {
      const firstDay = new Date(base);
      firstDay.setDate(base.getDate() + wi * 7);
      if (firstDay.getFullYear() > year) break;

      const week = [];
      for (let di = 0; di < 7; di++) {
        const d = cellDateOf(base, wi, di);
        if (d.getFullYear() !== year) { week.push(0); continue; }
        const entry = dateMap.get(toDateStr(d));
        const count = entry ? entry.count : 0;
        total += count;
        week.push(Math.min(count, 3));
      }
      weeks.push(week);
    }

    yearsData[year] = { weeks, base, total, meta: `in ${year}` };
  }

  return { yearsData, dateMap, availableYears };
}

export function buildCellData(weeks, base, dateMap) {
  const out = {};
  weeks.forEach((week, wi) => {
    week.forEach((level, di) => {
      if (level > 0) {
        const d     = cellDateOf(base, wi, di);
        const entry = dateMap.get(toDateStr(d));
        if (entry) {
          out[`${wi}-${di}`] = {
            date: d.toLocaleDateString("en-US", { month: "short", day: "numeric" }),
            apps: entry.entries.map(e => ({
              company:        e.company,
              status:         capitalizeStatus(e.status),
              gmailMessageId: e.gmailMessageId ?? null,
            })),
          };
        }
      }
    });
  });
  return out;
}

export function buildMonthLabels(numWeeks, base) {
  let prevMonth = -1;
  return Array.from({ length: numWeeks }, (_, wi) => {
    const d = cellDateOf(base, wi, 0);
    const m = d.getMonth();
    if (m !== prevMonth) { prevMonth = m; return d.toLocaleDateString("en-US", { month: "short" }); }
    return "";
  });
}
