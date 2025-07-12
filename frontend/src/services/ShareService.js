import api from "../api/api";

const ShareService = {
    generatePdf: async (id) => {
        const response = await api.get(`/generatePdf/${id}`, { responseType: 'blob' });
        return response.data;
    },

    generateTxt: async (id) => {
        const response = await api.get(`/generateTxt/${id}`, { responseType: 'blob' });
        return response.data;
    },

    loadDeck: async (file, folderId) => {
        const formData = new FormData();
        formData.append("fileToLoad", file);
        formData.append("folderId", folderId);

        const response = await api.post('/loadDeckTxt', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data;
    },

    shareFolder: async (addresseeEmail, folderId, accessLevel) => {
        const response = await api.post('/folder/shareFolder', {
            addresseeEmail, folderId, accessLevel
        });
        return response.data;
    }
};

export default ShareService;