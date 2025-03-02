import api from "../api/api";
import mockData from "../mocks/mockData";

const isDevelopment = process.env.NODE_ENV === 'development';

const UserStatisticsService = {
    getUserStatistics: async () => {
        if (isDevelopment) return mockData.userStatisticsGetUserStatistics;
        // const whatIneed = {
        //     daysLearning: 15,
        //     longestStreak: 15,
        //     currentStreak: 15,
        //     allNewCards: 57,
        //     allLearningCards: 120,
        //     allRememberedCards: 217,
        //     login: [
        //         '10-12-2024', '11-12-2024', '14-12-2024'
        //     ]
        // }
        const response = await api.get(`/userStatistics/getUserStatistics`);
        return response.data;
    }
};

export default UserStatisticsService