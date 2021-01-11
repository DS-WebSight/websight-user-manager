import styled from 'styled-components';

import { colors } from 'websight-admin/theme';

export const DetailsFieldContainer = styled.div`
    margin: 10px 0 0 20px;
`;

export const DetailsLabelContainer = styled.span`
    display: inline-block;
    color: ${colors.darkGrey};
    padding: 0 5px 0 0;
    width: 80px;
    text-align: right;
    vertical-align: top;
`;

export const DetailsTagsContainer = styled.div`
    display: inline-block;
    margin-top: -4px;
`;

export const SectionContainer = styled.div`
    display: block;
    margin: 20px 0 0;
    width: 100%;
    min-width: 400px;
`;

export const SectionsContainer = styled.div`
    @media (max-width: 999px) {
        display: block;
    }
    @media (min-width: 1000px) {
        display: flex;
        flex-grow: 1;
    }
`;

export const SectionsGroupContainer = styled.div`
    display: block;
    width: 100%;
`;
