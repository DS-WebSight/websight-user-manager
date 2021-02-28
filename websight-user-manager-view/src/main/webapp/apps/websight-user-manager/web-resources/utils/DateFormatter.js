const MINUTE_MILLIS = 60000;
const HOUR_MILLIS = MINUTE_MILLIS * 60;
const WEEK_MILLIS = HOUR_MILLIS * 24 * 7;
const DAYS_IN_WEEK = 7;

export const toRelative = (date) => {
    if (!date) {
        return undefined;
    }
    const now = new Date();
    const duration = now - date;
    if (duration <= MINUTE_MILLIS) {
        return 'a moment ago';
    } else if (duration <= HOUR_MILLIS) {
        return toRelativeMinutes(duration);
    } else if (isSameDay(new Date(date), now)) {
        return toRelativeHours(duration);
    } else if (duration <= WEEK_MILLIS) {
        return toRelativeDays(date, now);
    }
}

const toRelativeMinutes = (duration) => {
    const minutes = Math.round(duration / MINUTE_MILLIS);
    return minutes > 1 ? `${minutes} minutes ago` : 'a minute ago';
}

const toRelativeHours = (duration) => {
    const hours = Math.round(duration / HOUR_MILLIS);
    return hours > 1 ? `${hours} hours ago` : 'an hour ago';
}

const toRelativeDays = (date, dateNow) => {
    const dateDayOffset = 24 * HOUR_MILLIS;

    for (let i = 1; i < DAYS_IN_WEEK; i++) {
        const dayBeforeDate = dateNow - dateDayOffset * i;
        if (isSameDay(new Date(date), new Date(dayBeforeDate))) {
            if (i === 1) {
                return 'yesterday';
            } else {
                return i + ' days ago';
            }
        }
    }
    return '1 week ago';
}

const isSameDay = (first, second) => {
    return (first.getFullYear() === second.getFullYear() &&
        first.getMonth() === second.getMonth() &&
        first.getDate() === second.getDate());
}